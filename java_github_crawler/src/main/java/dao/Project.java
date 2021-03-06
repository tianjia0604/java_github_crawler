package dao;

public class Project {
    // 项目名字
    private String name;
    // 项目主页链接
    private String url;
    // 项目的描述信息
    private String description;

    // 以下属性是我们需要统计到的数据
    // 需要根据该项目的 url 进入到对应页面, 从页面上获取到刚才的这几个属性
    private int starCount;
    private int forkCount;
    private int openedIssueCount;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public int getForkCount() {
        return forkCount;
    }

    public void setForkCount(int forkCount) {
        this.forkCount = forkCount;
    }

    public int getOpenedIssueCount() {
        return openedIssueCount;
    }

    public void setOpenedIssueCount(int openedIssueCount) {
        this.openedIssueCount = openedIssueCount;
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", starCount=" + starCount +
                ", forkCount=" + forkCount +
                ", openedIssueCount=" + openedIssueCount +
                '}';
    }
}
