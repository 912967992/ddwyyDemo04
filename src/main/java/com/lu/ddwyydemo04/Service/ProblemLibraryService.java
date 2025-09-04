package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.ProblemLibraryDao;
import com.lu.ddwyydemo04.pojo.TestIssues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProblemLibraryService {

    @Autowired
    private ProblemLibraryDao problemLibraryDao;

    /**
     * 获取所有问题点
     * @return 问题点列表
     */
    public List<TestIssues> getAllProblems() {
        return problemLibraryDao.getAllProblems();
    }

    /**
     * 根据条件搜索问题点
     * @param filters 搜索条件
     * @return 问题点列表
     */
    public List<TestIssues> searchProblems(Map<String, Object> filters) {
        System.out.println("=== 搜索参数调试 ===");
        System.out.println("所有筛选参数: " + filters);
        System.out.println("当前状态筛选值: " + filters.get("currentStatus"));
        System.out.println("==================");
        return problemLibraryDao.searchProblems(filters);
    }

    /**
     * 更新问题点信息
     * @param testIssues 问题点对象
     * @return 是否更新成功
     */
    public boolean updateProblem(TestIssues testIssues) {
        // 设置修改时间
        testIssues.setModify_at(LocalDateTime.now());

        int result = problemLibraryDao.updateProblem(testIssues);
        return result > 0;
    }

    /**
     * 根据ID获取问题点详情
     * @param id 问题点ID
     * @return 问题点对象
     */
    public TestIssues getProblemById(Long id) {
        return problemLibraryDao.getProblemById(id);
    }

    /**
     * 统计问题点数量
     * @param filters 筛选条件
     * @return 数量
     */
    public int countProblems(Map<String, Object> filters) {
        return problemLibraryDao.countProblems(filters);
    }

    /**
     * 获取问题点统计信息
     * @param problems 问题点列表
     * @return 统计信息
     */
    public Map<String, Integer> getStatistics(List<TestIssues> problems) {
        Map<String, Integer> stats = new HashMap<>();
        int total = problems.size();
        int open = 0;
        int inProgress = 0;
        int resolved = 0;

        for (TestIssues problem : problems) {
            String status = problem.getCurrent_status();
            if ("Open".equals(status)) {
                open++;
            } else if ("In Progress".equals(status)) {
                inProgress++;
            } else if ("Resolved".equals(status)) {
                resolved++;
            }
        }

        stats.put("total", total);
        stats.put("open", open);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);

        return stats;
    }

    /**
     * 获取指定sample_id的历史版本
     * @param sampleId 样品ID
     * @return 历史版本列表
     */
    public List<TestIssues> getHistoryVersions(String sampleId) {
        return problemLibraryDao.getHistoryVersions(sampleId);
    }

    /**
     * 获取指定sample_id的最新版本
     * @param sampleId 样品ID
     * @return 最新版本
     */
    public TestIssues getLatestVersion(String sampleId) {
        return problemLibraryDao.getLatestVersion(sampleId);
    }

    /**
     * 检查是否存在历史版本
     * @param sampleId 样品ID
     * @return 是否有历史版本
     */
    public boolean hasHistoryVersions(String sampleId) {
        List<TestIssues> historyVersions = getHistoryVersions(sampleId);
        return historyVersions != null && historyVersions.size() > 1;
    }
}
