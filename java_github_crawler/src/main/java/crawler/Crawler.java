package crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dao.Project;
import dao.ProjectDao;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Crawler {
    private OkHttpClient okHttpClient = new OkHttpClient();
    private Gson gson = new GsonBuilder().create();

    private HashSet<String> urlBlackList = new HashSet<>();

    {
        urlBlackList.add("https://github.com/events");
        urlBlackList.add("https://github.community");
        urlBlackList.add("https://github.com/about");
        urlBlackList.add("https://github.com/pricing");
        urlBlackList.add("https://github.com/contact");
    }

    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler();

        long startTime = System.currentTimeMillis();

        // 1. 获取入口页面
        String html = crawler.getPage("https://github.com/akullpp/awesome-java/blob/master/README.md");
        // System.out.println(respBody);

        long finishTime = System.currentTimeMillis();
        // 大约 4s
        System.out.println("获取入口页面时间: " + (finishTime - startTime) + " ms");

        // 2. 解析入口页面, 获取项目列表
        List<Project> projects = crawler.parseProjectList(html);
        // System.out.println(projects);

        System.out.println("解析项目列表时间: " + (System.currentTimeMillis() - finishTime) + " ms");
        // 大约是 0.3 s
        finishTime = System.currentTimeMillis();

        // 3. 遍历项目列表, 调用 github API 获取项目信息
        for (int i = 0; i < projects.size() && i < 5; i++) {
            try {
                Project project = projects.get(i);
                System.out.println("crawing " + project.getName() + " ...");
                String repoName = crawler.getRepoName(project.getUrl());
                String jsonString = crawler.getRepoInfo(repoName);
                // System.out.println(jsonString);
                // 4. 解析每个仓库获取到的 JSON 数据, 得到需要的信息
                crawler.parseRepoInfo(jsonString, project);
                System.out.println("crawing " + project.getName() + " done!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 这个时间 138884 => 138s => 2min
        System.out.println("解析所有项目的时间: " + (System.currentTimeMillis() - finishTime) + " ms");
        finishTime = System.currentTimeMillis();

        // 5. 在这个位置把 project 保存到数据库中
        ProjectDao projectDao = new ProjectDao();
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            projectDao.save(project);
        }

        System.out.println("存储数据库的时间: " + (System.currentTimeMillis() - finishTime) + " ms");
        finishTime = System.currentTimeMillis();
        // 总时间: 147s
        System.out.println("整个项目的总时间: " + (finishTime - startTime) + " ms");
    }

    public String getPage(String url) throws IOException {

        //创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建一个请求对象
        Request request = new Request.Builder().url(url).build();
        //创建一个call对象，进行网络访问操作
        Call call = okHttpClient.newCall(request);
        //发送请求给服务器
        Response response = call.execute();
        if (!response.isSuccessful()) {
            System.out.println("请求失败");
            return null;
        }
        return response.body().string();
    }

    public List<Project> parseProjectList(String html) {
        ArrayList<Project> result = new ArrayList<>();
        // 1. 创建 Document 对象
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByTag("li");
        for (Element li : elements) {
            // 再去获取里面的 a 标签.
            Elements allLink = li.getElementsByTag("a");
            if (allLink.size() == 0) {
                // 当前的这个 li 标签中没有包含 a 标签. 直接忽略掉这个 li
                continue;
            }
            Element link = allLink.get(0);
            String url = link.attr("href");
            if (!url.startsWith("https://github.com")) {
                // 如果当前这个项目的 url 不是以 https://github.com 开头的, 我们就直接丢弃掉
                continue;
            }
            if (urlBlackList.contains(url)) {
                continue;
            }
            Project project = new Project();
            project.setName(link.text());
            project.setUrl(link.attr("href"));
            project.setDescription(li.text());
            result.add(project);
        }
        return result;
    }

    //调用 Github API 获取指定仓库信息
    // repoName 形如 doov-io/doov
    public String getRepoInfo(String repoName) throws IOException {
        String userName = "tianjia0604";
        String password = "tj999421";
        // 进行身份认证, 把用户名密码加密之后, 得到一个字符串, 把这个字符串放到 HTTP header 中.
        // 此处只是针对用户名密码进行了 base64 加密. 好过直接传输明文
        String credential = Credentials.basic(userName, password);

        String url = "https://api.github.com/repos/" + repoName;
        // OkHttpClient 对象前面已经创建过了, 不需要重复创建.
        // 请求对象, Call 对象, 响应对象, 还是需要重新创建的
        Request request = new Request.Builder().url(url).header("Authorization", credential).build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            System.out.println("访问失败 url" + url);
            return null;
        }
        return response.body().string();
    }
    //把项目中url提取出来其中仓库名字和作者名字
    public String getRepoName(String url) {
        int lastOne = url.lastIndexOf("/");
        int lastTwo = url.lastIndexOf("/", lastOne - 1);
        if (lastOne == -1 || lastTwo == -1) {
            System.out.println("当前 URL 不是一个标准的项目 url! url:" + url);
            return null;
        }
        return url.substring(lastTwo + 1);

    }
    //通过这个方法，获取到该仓库信息
    public void parseRepoInfo(String jsonString, Project project) {
        Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
        HashMap<String, Object> hashMap = gson.fromJson(jsonString, type);
        // hashMap 中的 key 的名字都是源于 Github API 的返回值.
        Double starCount = (Double)hashMap.get("stargazers_count");
        project.setStarCount(starCount.intValue());
        Double forkCount = (Double)hashMap.get("forks_count");
        project.setForkCount(forkCount.intValue());
        Double openedIssueCount = (Double)hashMap.get("open_issues_count");
        project.setOpenedIssueCount(openedIssueCount.intValue());
    }
}
