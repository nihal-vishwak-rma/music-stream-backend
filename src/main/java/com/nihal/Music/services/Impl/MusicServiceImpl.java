package com.nihal.Music.services.Impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nihal.Music.dtos.SongDto;
import com.nihal.Music.dtos.SongResponse;
import com.nihal.Music.dtos.StreamUrlDto;
import com.nihal.Music.entity.Song;
import com.nihal.Music.exception.AudioNotFoundException;
import com.nihal.Music.exception.InvalidVideoUrl;
import com.nihal.Music.repositories.SongRepository;
import com.nihal.Music.services.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MusicServiceImpl implements MusicService {

    private final SongRepository songRepository;
    private final Map<String, Page> pageCache = new ConcurrentHashMap<>();
    private final Cache<String, org.schabi.newpipe.extractor.stream.StreamInfo> streamCache = CacheBuilder.newBuilder()
            .maximumSize(500).expireAfterWrite(1, TimeUnit.HOURS).build();
    @Value("${youtube.data.api.key}")
    private String apikey;
    private YouTube youTube;


    public MusicServiceImpl(SongRepository songRepository) throws GeneralSecurityException, IOException {
        this.youTube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null

        )
                .setApplicationName("musicwebapp")
                .build();
        this.songRepository = songRepository;
    }

    @Override
    public SongResponse searchSongs(String query, String pagetoken) throws IOException {

        try {

            YouTube.Search.List search = youTube.search().list(Collections.singletonList("id,snippet"));
            search.setKey(apikey);
            search.setQ(query);
            search.setType(Collections.singletonList("video"));
            search.setVideoCategoryId("10");
            search.setMaxResults(30L);
            search.setFields(
                    "items(id/videoId,snippet(title,channelTitle,thumbnails/high/url)),nextPageToken,prevPageToken");

            if (pagetoken != null && !pagetoken.isEmpty()) {
                search.setPageToken(pagetoken);
            }

            SearchListResponse searchListResponse = search.execute();

            List<SearchResult> searchResults = searchListResponse.getItems();

            if (searchResults == null || searchResults.isEmpty()) {
                return new SongResponse(new ArrayList<>(), null, null);
            }

            List<String> videoIds = new ArrayList<>();

            for (SearchResult result : searchResults) {
                if (result.getId() != null && result.getId().getVideoId() != null) {
                    videoIds.add(result.getId().getVideoId());
                }
            }

            if (videoIds.isEmpty()) {
                return new SongResponse(new ArrayList<>(), null, null);
            }

            YouTube.Videos.List videoRequest = youTube.videos().list(Collections.singletonList("contentDetails"));
            videoRequest.setKey(apikey);
            videoRequest.setId(videoIds);
            videoRequest.setFields("items(id,contentDetails/duration)");

            VideoListResponse videoListResponse = videoRequest.execute();
            List<Video> videos = videoListResponse.getItems();

            if (videos == null) {
                videos = new ArrayList<>();
            }

            List<SongDto> songs = new ArrayList<>();

            for (int i = 0; i < searchResults.size(); i++) {

                SearchResult searchResult = searchResults.get(i);

                if (searchResult.getId().getVideoId() == null) {
                    continue;
                }

                Video video = i < videos.size() ? videos.get(i) : null;
                String duration = video != null ? video.getContentDetails().getDuration() : "PT0S";

                String formattedDuration = convertDuration(duration);

                Duration parsed = Duration.parse(duration);

                if (parsed.toMinutes() == 0 && parsed.getSeconds() <= 90) {
                    continue;
                }

                SongDto songDto = new SongDto();
                songDto.setVideoId(searchResult.getId().getVideoId());
                songDto.setArtist(searchResult.getSnippet().getChannelTitle());
                songDto.setTitle(searchResult.getSnippet().getTitle());
                songDto.setDuration(formattedDuration);
                songDto.setThumbnailUrl(searchResult.getSnippet().getThumbnails().getHigh().getUrl());

                songs.add(songDto);

            }

            SongResponse songResponse = new SongResponse(songs, searchListResponse.getNextPageToken(),
                    searchListResponse.getPrevPageToken());

            return songResponse;

        } catch (GoogleJsonResponseException e) {

            if (e.getStatusCode() == 403 && e.getDetails() != null &&
                    "quotaExceeded".equalsIgnoreCase(e.getDetails().getErrors().get(0).getReason())) {

                log.warn("YouTube API quota exceeded! Falling back to NewPipe extractor...");

                return searchSongNewPipe(query, pagetoken, 20);
            }

            throw e;

        }
    }

    public String convertDuration(String isoDuration) {
        Duration duration = Duration.parse(isoDuration);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }

    public Song fetchAndSaveSong(String videoId) throws IOException {

        YouTube.Videos.List myvideo = youTube.videos()
                .list(Collections.singletonList("snippet,contentDetails,statistics"));

        myvideo.setKey(apikey);
        myvideo.setId(Collections.singletonList(videoId));
        myvideo.setFields(
                "items(id,snippet(title,channelTitle,thumbnails),contentDetails(duration),statistics(viewCount))");

        VideoListResponse videoListResponse = myvideo.execute();
        List<Video> videos = videoListResponse.getItems();

        if (videos.isEmpty()) {
            throw new IllegalArgumentException("Video not found for id: " + videoId);
        }

        Video video = videos.get(0);

        String thumbnailUrl = Optional.ofNullable(video.getSnippet())
                .map(VideoSnippet::getThumbnails)
                .map(ThumbnailDetails::getHigh)
                .map(Thumbnail::getUrl)
                .orElse(null);

        Song song = Song.builder()
                .videoId(videoId)
                .title(video.getSnippet().getTitle())
                .artist(video.getSnippet().getChannelTitle())
                .duration(convertDuration(video.getContentDetails().getDuration()))
                .thumbnailUrl(thumbnailUrl)
                .build();

        return songRepository.save(song);

    }


    @Override
    public SongResponse searchSongNewPipe(String query, String pageToken, int limit) {

        log.info("Searching songs for query: {}", query);

        try {
            StreamingService youtubeService = NewPipe.getService(0);
            SearchExtractor extractor = youtubeService.getSearchExtractor(query);
            extractor.fetchPage();

            InfoItemsPage<InfoItem> infoItemsPage = fetchPage(extractor, pageToken);

            List<SongDto> songDtos = processSongs(infoItemsPage.getItems(), limit);

            String nextToken = cacheNextPage(infoItemsPage.getNextPage());

            log.info("Successfully fetched {} songs", songDtos.size());

            return new SongResponse(songDtos, nextToken, pageToken);

        } catch (ExtractionException e) {
            log.error("Extraction error while searching: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract songs from YouTube", e);
        } catch (IOException e) {
            log.error("IO error while fetching page: {}", e.getMessage(), e);
            throw new RuntimeException("Network error while fetching songs", e);
        } catch (Exception e) {
            log.error("Unexpected error during search: {}", e.getMessage(), e);
            return new SongResponse(new ArrayList<>(), null, null);
        }
    }


    private InfoItemsPage<InfoItem> fetchPage(SearchExtractor extractor, String pageToken)
            throws IOException, ExtractionException {

        if (pageToken == null || pageToken.isEmpty()) {
            return extractor.getInitialPage();
        }

        Page page = pageCache.get(pageToken);
        if (page == null) {
            log.warn("Invalid or expired page token: {}", pageToken);
            throw new IllegalArgumentException("Invalid or expired page token");
        }

        return extractor.getPage(page);
    }

    private List<SongDto> processSongs(List<InfoItem> items, int limit) {
        return items.stream()
                .filter(item -> item instanceof StreamInfoItem)
                .map(item -> (StreamInfoItem) item)
                .filter(this::isValidSong)
                .limit(limit)
                .map(this::convertToSongDto)
                .collect(Collectors.toList());
    }

    private boolean isValidSong(StreamInfoItem streamItem) {
        long duration = streamItem.getDuration();

        if (duration < 60 || duration > 600) {
            log.debug("Skipping song '{}' with invalid duration: {}s",
                    streamItem.getName(), duration);
            return false;
        }

        return true;
    }

    private SongDto convertToSongDto(StreamInfoItem streamItem) {
        SongDto songDto = new SongDto();

        songDto.setId(null);
        songDto.setTitle(Optional.ofNullable(streamItem.getName())
                .orElse("Unknown Title"));
        songDto.setVideoId(extractVideoIdFromUrl(streamItem.getUrl()));
        songDto.setDuration(formatDuration(streamItem.getDuration()));
        songDto.setArtist(Optional.ofNullable(streamItem.getUploaderName())
                .orElse("Unknown Artist"));
        songDto.setThumbnailUrl(getThumbnailUrl(streamItem.getThumbnails()));

        return songDto;
    }

    private String getThumbnailUrl(List<Image> thumbnails) {
        if (thumbnails == null || thumbnails.isEmpty()) {
            return "";
        }

        return thumbnails.get(thumbnails.size() - 1).getUrl();
    }

    private String cacheNextPage(Page nextPage) {
        if (nextPage == null) {
            return null;
        }

        String tokenKey = UUID.randomUUID().toString();
        pageCache.put(tokenKey, nextPage);


        return tokenKey;
    }

    private String extractVideoIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        try {

            Pattern youtubePattern = Pattern.compile(
                    "(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})"
            );

            Matcher matcher = youtubePattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }

            log.warn("Could not extract video ID from URL: {}", url);
            return "";

        } catch (Exception e) {
            log.error("Error extracting video ID from URL: {}", url, e);
            return "";
        }
    }

    private String formatDuration(long durationSeconds) {
        if (durationSeconds <= 0) {
            return "Live/Unknown";
        }

        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;

        return hours > 0
                ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                : String.format("%d:%02d", minutes, seconds);
    }


    @Override
    public StreamUrlDto extractStreamUrl(String videoId, String quality) {

        try {

            log.info("extracting streamUrl...");

            if (videoId == null || videoId.trim().isEmpty()) {
                throw new InvalidVideoUrl("invalid video id");
            }


            StreamingService youtubeservice = NewPipe.getService(0);

            String fullUrl = "https://www.youtube.com/watch?v=" + videoId;

            StreamInfo streamInfo = streamCache.getIfPresent(videoId);

            if (streamInfo == null) {
                streamInfo = StreamInfo.getInfo(youtubeservice, fullUrl);
                streamCache.put(videoId, streamInfo);
            }

            List<AudioStream> audioStreams = streamInfo.getAudioStreams();

            if (audioStreams.isEmpty()) {
                throw new AudioNotFoundException("\"No audio stream found for this video.\"");
            }

            AudioStream audioStream = selectAudioStream(audioStreams, quality);

            if (audioStream == null || audioStream.getUrl() == null) {
                throw new AudioNotFoundException("Playable audio URL not found");
            }

            log.info("Extracted audio url");
            return new StreamUrlDto(audioStream.getUrl());
        } catch (AudioNotFoundException | InvalidVideoUrl e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract stream url", e);
            throw new RuntimeException("Failed to extract audio stream");
        }

    }

    private AudioStream selectAudioStream(List<AudioStream> streams, String quality) {
        return switch (quality.toLowerCase()) {
            case "high" -> streams.stream()
                    .max(Comparator.comparing(AudioStream::getAverageBitrate))
                    .orElse(streams.get(0));
            case "low" -> streams.stream()
                    .min(Comparator.comparing(AudioStream::getAverageBitrate))
                    .orElse(streams.get(0));
            default -> streams.stream()
                    .sorted(Comparator.comparing(AudioStream::getAverageBitrate))
                    .skip(streams.size() / 2)
                    .findFirst()
                    .orElse(streams.get(0));
        };
    }


}
