package net.doge.sdk.entity.album.search;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.doge.constant.async.GlobalExecutors;
import net.doge.constant.system.NetMusicSource;
import net.doge.model.entity.NetAlbumInfo;
import net.doge.sdk.common.CommonResult;
import net.doge.sdk.common.SdkCommon;
import net.doge.sdk.util.SdkUtil;
import net.doge.util.collection.ListUtil;
import net.doge.util.common.StringUtil;
import net.doge.util.common.TimeUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class AlbumSearchReq {
    // 关键词搜索专辑 API
    private final String SEARCH_ALBUM_API = SdkCommon.prefix + "/cloudsearch?type=10&keywords=%s&limit=%s&offset=%s";
    // 关键词搜索专辑 API (酷狗)
    private final String SEARCH_ALBUM_KG_API = "http://msearch.kugou.com/api/v3/search/album?keyword=%s&page=%s&pagesize=%s";
    private final String SEARCH_ALBUM_KW_API = "http://www.kuwo.cn/api/www/search/searchAlbumBykeyWord?key=%s&pn=%s&rn=%s&httpsStatus=1";
    // 关键词搜索专辑 API (咪咕)
    private final String SEARCH_ALBUM_MG_API = SdkCommon.prefixMg + "/search?type=album&keyword=%s&pageNo=%s&pageSize=%s";
    // 关键词搜索专辑 API (千千)
    private final String SEARCH_ALBUM_QI_API = "https://music.91q.com/v1/search?appid=16073360&pageNo=%s&pageSize=%s&timestamp=%s&type=3&word=%s";
    // 关键词搜索专辑 API (豆瓣)
    private final String SEARCH_ALBUM_DB_API = "https://www.douban.com/j/search?q=%s&start=%s&cat=1003";
    // 关键词搜索专辑 API (堆糖)
    private final String SEARCH_ALBUM_DT_API
            = "https://www.duitang.com/napi/album/list/by_search/?include_fields=is_root,source_link,item,buyable,root_id,status,like_count,sender,album,cover" +
            "&kw=%s&start=%s&limit=%s&type=album&_type=&_=%s";
    // 关键词搜索专辑 API 2 (堆糖)
    private final String SEARCH_ALBUM_DT_API_2
            = "https://www.duitang.com/napi/blogv2/list/by_search/?include_fields=is_root,source_link,item,buyable,root_id,status,like_count,sender,album,cover" +
            "&kw=%s&start=%s&limit=%s&type=feed&_type=&_=%s";

    /**
     * 根据关键词获取专辑
     */
    public CommonResult<NetAlbumInfo> searchAlbums(int src, String keyword, int limit, int page) {
        AtomicInteger total = new AtomicInteger();
        List<NetAlbumInfo> albumInfos = new LinkedList<>();

        // 先对关键词编码，避免特殊符号的干扰
        String encodedKeyword = StringUtil.urlEncode(keyword);

        // 网易云
        Callable<CommonResult<NetAlbumInfo>> searchAlbums = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            String albumInfoBody = HttpRequest.get(String.format(SEARCH_ALBUM_API, encodedKeyword, limit, (page - 1) * limit))
                    .execute()
                    .body();
            JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
            JSONObject result = albumInfoJson.getJSONObject("result");
            if (!result.containsKey("albums")) return new CommonResult<>(albumInfos, 0);
            t = result.getIntValue("albumCount");
            JSONArray albumArray = result.getJSONArray("albums");
            for (int i = 0, len = albumArray.size(); i < len; i++) {
                JSONObject albumJson = albumArray.getJSONObject(i);

                String albumId = albumJson.getString("id");
                String albumName = albumJson.getString("name");
                String artist = SdkUtil.parseArtists(albumJson, NetMusicSource.NET_CLOUD);
                String artistId = albumJson.getJSONArray("artists").getJSONObject(0).getString("id");
                String publishTime = TimeUtil.msToDate(albumJson.getLong("publishTime"));
                Integer songNum = albumJson.getIntValue("size");
                String coverImgThumbUrl = albumJson.getString("picUrl");

                NetAlbumInfo albumInfo = new NetAlbumInfo();
                albumInfo.setId(albumId);
                albumInfo.setName(albumName);
                albumInfo.setArtist(artist);
                albumInfo.setArtistId(artistId);
                albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                albumInfo.setPublishTime(publishTime);
                albumInfo.setSongNum(songNum);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    albumInfo.setCoverImgThumb(coverImgThumb);
                });
                res.add(albumInfo);
            }
            return new CommonResult<>(res, t);
        };

        // 酷狗
        Callable<CommonResult<NetAlbumInfo>> searchAlbumsKg = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            String albumInfoBody = HttpRequest.get(String.format(SEARCH_ALBUM_KG_API, encodedKeyword, page, limit))
                    .execute()
                    .body();
            JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
            JSONObject data = albumInfoJson.getJSONObject("data");
            t = data.getIntValue("total");
            JSONArray albumArray = data.getJSONArray("info");
            for (int i = 0, len = albumArray.size(); i < len; i++) {
                JSONObject albumJson = albumArray.getJSONObject(i);

                String albumId = albumJson.getString("albumid");
                String albumName = albumJson.getString("albumname");
                String artist = albumJson.getString("singername");
                String artistId = albumJson.getString("singerid");
                String publishTime = albumJson.getString("publishtime").replace(" 00:00:00", "");
                Integer songNum = albumJson.getIntValue("songcount");
                String coverImgThumbUrl = albumJson.getString("imgurl").replace("/{size}", "");

                NetAlbumInfo albumInfo = new NetAlbumInfo();
                albumInfo.setSource(NetMusicSource.KG);
                albumInfo.setId(albumId);
                albumInfo.setName(albumName);
                albumInfo.setArtist(artist);
                albumInfo.setArtistId(artistId);
                albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                albumInfo.setPublishTime(publishTime);
                albumInfo.setSongNum(songNum);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    albumInfo.setCoverImgThumb(coverImgThumb);
                });
                res.add(albumInfo);
            }
            return new CommonResult<>(res, t);
        };

        // QQ
        Callable<CommonResult<NetAlbumInfo>> searchAlbumsQq = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            String albumInfoBody = HttpRequest.post(String.format(SdkCommon.qqSearchApi))
                    .body(String.format(SdkCommon.qqSearchJson, page, limit, keyword, 2))
                    .execute()
                    .body();
            JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
            JSONObject data = albumInfoJson.getJSONObject("music.search.SearchCgiService").getJSONObject("data");
            t = data.getJSONObject("meta").getIntValue("sum");
            JSONArray albumArray = data.getJSONObject("body").getJSONObject("album").getJSONArray("list");
            for (int i = 0, len = albumArray.size(); i < len; i++) {
                JSONObject albumJson = albumArray.getJSONObject(i);

                String albumId = albumJson.getString("albumMID");
                String albumName = albumJson.getString("albumName");
                String artist = SdkUtil.parseArtists(albumJson, NetMusicSource.QQ);
                String artistId = albumJson.getJSONArray("singer_list").getJSONObject(0).getString("mid");
                String publishTime = albumJson.getString("publicTime");
                Integer songNum = albumJson.getIntValue("song_count");
                String coverImgThumbUrl = albumJson.getString("albumPic").replaceFirst("http:", "https:");

                NetAlbumInfo albumInfo = new NetAlbumInfo();
                albumInfo.setSource(NetMusicSource.QQ);
                albumInfo.setId(albumId);
                albumInfo.setName(albumName);
                albumInfo.setArtist(artist);
                albumInfo.setArtistId(artistId);
                albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                albumInfo.setPublishTime(publishTime);
                albumInfo.setSongNum(songNum);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    albumInfo.setCoverImgThumb(coverImgThumb);
                });
                res.add(albumInfo);
            }
            return new CommonResult<>(res, t);
        };

        // 酷我
        Callable<CommonResult<NetAlbumInfo>> searchAlbumsKw = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            HttpResponse resp = SdkCommon.kwRequest(String.format(SEARCH_ALBUM_KW_API, encodedKeyword, page, limit)).execute();
            if (resp.getStatus() == HttpStatus.HTTP_OK) {
                String albumInfoBody = resp.body();
                JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
                JSONObject data = albumInfoJson.getJSONObject("data");
                t = data.getIntValue("total");
                JSONArray albumArray = data.getJSONArray("albumList");
                for (int i = 0, len = albumArray.size(); i < len; i++) {
                    JSONObject albumJson = albumArray.getJSONObject(i);

                    String albumId = albumJson.getString("albumid");
                    String albumName = StringUtil.removeHTMLLabel(albumJson.getString("album"));
                    String artist = albumJson.getString("artist");
                    String artistId = albumJson.getString("artistid");
                    String publishTime = albumJson.getString("releaseDate");
                    String coverImgThumbUrl = albumJson.getString("pic");

                    NetAlbumInfo albumInfo = new NetAlbumInfo();
                    albumInfo.setSource(NetMusicSource.KW);
                    albumInfo.setId(albumId);
                    albumInfo.setName(albumName);
                    albumInfo.setArtist(artist);
                    albumInfo.setArtistId(artistId);
                    albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                    albumInfo.setPublishTime(publishTime);
                    GlobalExecutors.imageExecutor.execute(() -> {
                        BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                        albumInfo.setCoverImgThumb(coverImgThumb);
                    });
                    res.add(albumInfo);
                }
            }
            return new CommonResult<>(res, t);
        };

        // 咪咕
        Callable<CommonResult<NetAlbumInfo>> searchAlbumsMg = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            String albumInfoBody = HttpRequest.get(String.format(SEARCH_ALBUM_MG_API, encodedKeyword, page, limit))
                    .execute()
                    .body();
            JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
            // 咪咕可能接口异常，需要判空！
            JSONObject data = albumInfoJson.getJSONObject("data");
            if (data != null) {
                t = data.getIntValue("total");
                JSONArray albumArray = data.getJSONArray("list");
                for (int i = 0, len = albumArray.size(); i < len; i++) {
                    JSONObject albumJson = albumArray.getJSONObject(i);

                    String albumId = albumJson.getString("id");
                    String albumName = albumJson.getString("name");
                    String artist = SdkUtil.parseArtists(albumJson, NetMusicSource.MG);
                    String artistId = albumJson.getJSONArray("artists").getJSONObject(0).getString("id");
                    String publishTime = albumJson.getString("publishTime");
                    Integer songNum = albumJson.getIntValue("songCount");
                    String coverImgThumbUrl = albumJson.getString("picUrl");

                    NetAlbumInfo albumInfo = new NetAlbumInfo();
                    albumInfo.setSource(NetMusicSource.MG);
                    albumInfo.setId(albumId);
                    albumInfo.setName(albumName);
                    albumInfo.setArtist(artist);
                    albumInfo.setArtistId(artistId);
                    albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                    albumInfo.setPublishTime(publishTime);
                    albumInfo.setSongNum(songNum);
                    GlobalExecutors.imageExecutor.execute(() -> {
                        BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                        albumInfo.setCoverImgThumb(coverImgThumb);
                    });
                    res.add(albumInfo);
                }
            }
            return new CommonResult<>(res, t);
        };

        // 千千
        Callable<CommonResult<NetAlbumInfo>> searchAlbumsQi = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            HttpResponse resp = HttpRequest.get(SdkCommon.buildQianUrl(String.format(SEARCH_ALBUM_QI_API, page, limit, System.currentTimeMillis(), encodedKeyword))).execute();
            String albumInfoBody = resp.body();
            JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
            JSONObject data = albumInfoJson.getJSONObject("data");
            t = data.getIntValue("total");
            JSONArray albumArray = data.getJSONArray("typeAlbum");
            for (int i = 0, len = albumArray.size(); i < len; i++) {
                JSONObject albumJson = albumArray.getJSONObject(i);

                String albumId = albumJson.getString("albumAssetCode");
                String albumName = albumJson.getString("title");
                String artist = SdkUtil.parseArtists(albumJson, NetMusicSource.QI);
                JSONArray artistArray = albumJson.getJSONArray("artist");
                String artistId = artistArray != null && !artistArray.isEmpty() ? artistArray.getJSONObject(0).getString("artistCode") : "";
                String rd = albumJson.getString("releaseDate");
                String publishTime = StringUtil.notEmpty(rd) ? rd.split("T")[0] : "";
                String coverImgThumbUrl = albumJson.getString("pic");
                Integer songNum = albumJson.getJSONArray("trackList").size();

                NetAlbumInfo albumInfo = new NetAlbumInfo();
                albumInfo.setSource(NetMusicSource.QI);
                albumInfo.setId(albumId);
                albumInfo.setName(albumName);
                albumInfo.setArtist(artist);
                albumInfo.setArtistId(artistId);
                albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                albumInfo.setPublishTime(publishTime);
                albumInfo.setSongNum(songNum);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    albumInfo.setCoverImgThumb(coverImgThumb);
                });
                res.add(albumInfo);
            }
            return new CommonResult<>(res, t);
        };

        // 豆瓣
        Callable<CommonResult<NetAlbumInfo>> searchAlbumsDb = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            final int lim = Math.min(20, limit);
            String albumInfoBody = HttpRequest.get(String.format(SEARCH_ALBUM_DB_API, encodedKeyword, (page - 1) * lim))
                    .execute()
                    .body();
            JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
            JSONArray albumArray = albumInfoJson.getJSONArray("items");
            if (albumArray != null) {
                int to = albumInfoJson.getIntValue("total");
                t = (to % lim == 0 ? to / lim : to / lim + 1) * limit;
                for (int i = 0, len = albumArray.size(); i < len; i++) {
                    Document doc = Jsoup.parse(albumArray.getString(i));
                    Elements result = doc.select("div.result");
                    Elements a = result.select("h3 a");

                    String albumId = ReUtil.get("sid: (\\d+)", a.attr("onclick"), 1);
                    String albumName = a.text().trim();
                    String artist = result.select("span.subject-cast").text();
                    String coverImgThumbUrl = result.select("div.pic img").attr("src");

                    NetAlbumInfo albumInfo = new NetAlbumInfo();
                    albumInfo.setSource(NetMusicSource.DB);
                    albumInfo.setId(albumId);
                    albumInfo.setName(albumName);
                    albumInfo.setArtist(artist);
                    albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                    GlobalExecutors.imageExecutor.execute(() -> {
                        BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                        albumInfo.setCoverImgThumb(coverImgThumb);
                    });

                    res.add(albumInfo);
                }
            }
//            String albumInfoBody = HttpRequest.get(String.format(SEARCH_ALBUM_DB_API, encodedKeyword, (page - 1) * lim))
//                    .execute()
//                    .body();
//            Document doc = Jsoup.parse(albumInfoBody);
//            t = 4000 / lim * limit;
//            Elements result = doc.select("div.sc-bZQynM.hrvolz.sc-bxivhb.hvEfwz");
//            for (int i = 0, len = result.size(); i < len; i++) {
//                Element album = result.get(i);
//                Element a = album.select(".title a").first();
//                Element img = album.select(".item-root img").first();
//
//                String albumId = ReUtil.get("subject/(\\d+)/", a.attr("href"), 1);
//                String albumName = a.text();
//                String artist = album.select(".meta.abstract").text();
//                String coverImgThumbUrl = img.attr("src");
//
//                NetAlbumInfo albumInfo = new NetAlbumInfo();
//                albumInfo.setSource(NetMusicSource.DB);
//                albumInfo.setId(albumId);
//                albumInfo.setName(albumName);
//                albumInfo.setArtist(artist);
//                albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
//                GlobalExecutors.imageExecutor.execute(() -> {
//                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
//                    albumInfo.setCoverImgThumb(coverImgThumb);
//                });
//
//                res.add(albumInfo);
//            }
            return new CommonResult<>(res, t);
        };

        // 堆糖
        Callable<CommonResult<NetAlbumInfo>> searchAlbumsDt = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            HttpResponse resp = HttpRequest.get(String.format(SEARCH_ALBUM_DT_API, encodedKeyword, (page - 1) * limit, limit, System.currentTimeMillis())).execute();
            String albumInfoBody = resp.body();
            JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
            JSONObject data = albumInfoJson.getJSONObject("data");
            t = data.getIntValue("total");
            JSONArray albumArray = data.getJSONArray("object_list");
            for (int i = 0, len = albumArray.size(); i < len; i++) {
                JSONObject albumJson = albumArray.getJSONObject(i);

                String albumId = albumJson.getString("id");
                String albumName = albumJson.getString("name");
                String artist = albumJson.getJSONObject("user").getString("username");
                String artistId = albumJson.getJSONObject("user").getString("id");
                String publishTime = TimeUtil.msToDate(albumJson.getLong("updated_at_ts") * 1000);
                String coverImgThumbUrl = albumJson.getJSONArray("covers").getString(0);
                Integer songNum = albumJson.getIntValue("count");

                NetAlbumInfo albumInfo = new NetAlbumInfo();
                albumInfo.setSource(NetMusicSource.DT);
                albumInfo.setId(albumId);
                albumInfo.setName(albumName);
                albumInfo.setArtist(artist);
                albumInfo.setArtistId(artistId);
                albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                albumInfo.setPublishTime(publishTime);
                albumInfo.setSongNum(songNum);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    albumInfo.setCoverImgThumb(coverImgThumb);
                });
                res.add(albumInfo);
            }
            return new CommonResult<>(res, t);
        };
        Callable<CommonResult<NetAlbumInfo>> searchAlbumsDt2 = () -> {
            LinkedList<NetAlbumInfo> res = new LinkedList<>();
            Integer t = 0;

            HttpResponse resp = HttpRequest.get(String.format(SEARCH_ALBUM_DT_API_2, encodedKeyword, (page - 1) * limit, limit, System.currentTimeMillis())).execute();
            String albumInfoBody = resp.body();
            JSONObject albumInfoJson = JSONObject.parseObject(albumInfoBody);
            JSONObject data = albumInfoJson.getJSONObject("data");
            t = data.getIntValue("total");
            JSONArray albumArray = data.getJSONArray("object_list");
            for (int i = 0, len = albumArray.size(); i < len; i++) {
                JSONObject mainJson = albumArray.getJSONObject(i);
                JSONObject albumJson = mainJson.getJSONObject("album");

                String albumId = albumJson.getString("id");
                String albumName = albumJson.getString("name");
                String artist = mainJson.getJSONObject("sender").getString("username");
                String artistId = mainJson.getJSONObject("sender").getString("id");
                String publishTime = TimeUtil.msToDate(mainJson.getLong("add_datetime_ts") * 1000);
                String coverImgThumbUrl = albumJson.getJSONArray("covers").getString(0);
                Integer songNum = albumJson.getIntValue("count");

                NetAlbumInfo albumInfo = new NetAlbumInfo();
                albumInfo.setSource(NetMusicSource.DT);
                albumInfo.setId(albumId);
                albumInfo.setName(albumName);
                albumInfo.setArtist(artist);
                albumInfo.setArtistId(artistId);
                albumInfo.setCoverImgThumbUrl(coverImgThumbUrl);
                albumInfo.setPublishTime(publishTime);
                albumInfo.setSongNum(songNum);
                GlobalExecutors.imageExecutor.execute(() -> {
                    BufferedImage coverImgThumb = SdkUtil.extractCover(coverImgThumbUrl);
                    albumInfo.setCoverImgThumb(coverImgThumb);
                });
                res.add(albumInfo);
            }
            return new CommonResult<>(res, t);
        };

        List<Future<CommonResult<NetAlbumInfo>>> taskList = new LinkedList<>();

        if (src == NetMusicSource.NET_CLOUD || src == NetMusicSource.ALL)
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbums));
        if (src == NetMusicSource.KG || src == NetMusicSource.ALL)
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbumsKg));
        if (src == NetMusicSource.QQ || src == NetMusicSource.ALL)
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbumsQq));
        if (src == NetMusicSource.KW || src == NetMusicSource.ALL)
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbumsKw));
        if (src == NetMusicSource.MG || src == NetMusicSource.ALL)
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbumsMg));
        if (src == NetMusicSource.QI || src == NetMusicSource.ALL)
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbumsQi));
        if (src == NetMusicSource.DB || src == NetMusicSource.ALL)
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbumsDb));
        if (src == NetMusicSource.DT || src == NetMusicSource.ALL) {
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbumsDt));
            taskList.add(GlobalExecutors.requestExecutor.submit(searchAlbumsDt2));
        }

        List<List<NetAlbumInfo>> rl = new LinkedList<>();
        taskList.forEach(task -> {
            try {
                CommonResult<NetAlbumInfo> result = task.get();
                rl.add(result.data);
                total.set(Math.max(total.get(), result.total));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        albumInfos.addAll(ListUtil.joinAll(rl));

        return new CommonResult<>(albumInfos, total.get());
    }
}
