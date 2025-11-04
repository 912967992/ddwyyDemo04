package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.dao.ProblemLibraryDao;
import com.lu.ddwyydemo04.dao.SearchHistoryDao;
import com.lu.ddwyydemo04.pojo.SearchHistory;
import com.lu.ddwyydemo04.pojo.TestIssues;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    @Autowired
    private SearchHistoryDao searchHistoryDao;
    
    private ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 根据条件搜索问题点
     * @param filters 搜索条件
     * @return 问题点列表
     */
    public List<TestIssues> searchProblems(Map<String, Object> filters) {
        // 处理当前状态的兼容性筛选
        if (filters.containsKey("currentStatus") && filters.get("currentStatus") != null) {
            String currentStatus = (String) filters.get("currentStatus");
            if (!currentStatus.isEmpty()) {
                // 根据选择的状态，添加兼容的搜索条件
                if ("Closed".equals(currentStatus)) {
                    // Closed状态兼容 close, closed, Close, CLOSED
                    filters.put("currentStatusCompatible", "close,closed,Close,CLOSED");
                } else if ("Follow up".equals(currentStatus)) {
                    // Follow up状态兼容 follow up, followup, Follow up, FOLLOW UP
                    filters.put("currentStatusCompatible", "follow up,followup,Follow up,FOLLOW UP");
                }
                // Open状态不需要兼容处理，保持原值
            }
        }
        
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

    /**
     * 获取大类小类样品阶段完整编码版本问题类别测试人员责任部门测试平台显示设备DQE负责人选项
     * @return 包含大类、小类、样品阶段、完整编码、版本、问题类别、测试人员、责任部门、测试平台、显示设备和DQE负责人选项的Map
     */
    public Map<String, List<String>> getSpeciesOptions() {
        Map<String, Object> rawData = problemLibraryDao.getSpeciesOptions();
        Map<String, List<String>> result = new HashMap<>();
        
        // 处理大类选项
        String bigSpeciesStr = (String) rawData.get("bigSpecies");
        if (bigSpeciesStr != null && !bigSpeciesStr.isEmpty()) {
            List<String> bigSpeciesList = java.util.Arrays.asList(bigSpeciesStr.split(","));
            // 将"空值"转换为空字符串，用于前端显示和筛选
            bigSpeciesList = bigSpeciesList.stream()
                .map(value -> "空值".equals(value) ? "" : value)
                .collect(java.util.stream.Collectors.toList());
            result.put("bigSpecies", bigSpeciesList);
        } else {
            result.put("bigSpecies", new java.util.ArrayList<>());
        }
        
        // 处理小类选项
        String smallSpeciesStr = (String) rawData.get("smallSpecies");
        if (smallSpeciesStr != null && !smallSpeciesStr.isEmpty()) {
            List<String> smallSpeciesList = java.util.Arrays.asList(smallSpeciesStr.split(","));
            // 将"空值"转换为空字符串，用于前端显示和筛选
            smallSpeciesList = smallSpeciesList.stream()
                .map(value -> "空值".equals(value) ? "" : value)
                .collect(java.util.stream.Collectors.toList());
            result.put("smallSpecies", smallSpeciesList);
        } else {
            result.put("smallSpecies", new java.util.ArrayList<>());
        }
        
        // 处理样品阶段选项
        String sampleStageStr = (String) rawData.get("sampleStage");
        if (sampleStageStr != null && !sampleStageStr.isEmpty()) {
            result.put("sampleStage", java.util.Arrays.asList(sampleStageStr.split(",")));
        } else {
            result.put("sampleStage", new java.util.ArrayList<>());
        }
        
        // 处理完整编码选项
        String fullModelStr = (String) rawData.get("fullModel");
        if (fullModelStr != null && !fullModelStr.isEmpty()) {
            result.put("fullModel", java.util.Arrays.asList(fullModelStr.split(",")));
        } else {
            result.put("fullModel", new java.util.ArrayList<>());
        }
        
        // 处理版本选项
        String versionStr = (String) rawData.get("version");
        if (versionStr != null && !versionStr.isEmpty()) {
            result.put("version", java.util.Arrays.asList(versionStr.split(",")));
        } else {
            result.put("version", new java.util.ArrayList<>());
        }
        
        // 处理问题类别选项
        String problemCategoryStr = (String) rawData.get("problemCategory");
        if (problemCategoryStr != null && !problemCategoryStr.isEmpty()) {
            result.put("problemCategory", java.util.Arrays.asList(problemCategoryStr.split(",")));
        } else {
            result.put("problemCategory", new java.util.ArrayList<>());
        }
        
        // 处理测试人员选项
        String testerStr = (String) rawData.get("tester");
        if (testerStr != null && !testerStr.isEmpty()) {
            result.put("tester", java.util.Arrays.asList(testerStr.split(",")));
        } else {
            result.put("tester", new java.util.ArrayList<>());
        }
        
        // 处理责任部门选项
        String responsibleDepartmentStr = (String) rawData.get("responsibleDepartment");
        if (responsibleDepartmentStr != null && !responsibleDepartmentStr.isEmpty()) {
            result.put("responsibleDepartment", java.util.Arrays.asList(responsibleDepartmentStr.split(",")));
        } else {
            result.put("responsibleDepartment", new java.util.ArrayList<>());
        }
        
        // 处理测试平台选项
        String testPlatformStr = (String) rawData.get("testPlatform");
        if (testPlatformStr != null && !testPlatformStr.isEmpty()) {
            List<String> testPlatformList = java.util.Arrays.asList(testPlatformStr.split(","));
            // 将"空值"转换为空字符串，用于前端显示和筛选
            testPlatformList = testPlatformList.stream()
                .map(value -> "空值".equals(value) ? "" : value)
                .collect(java.util.stream.Collectors.toList());
            result.put("testPlatform", testPlatformList);
        } else {
            result.put("testPlatform", new java.util.ArrayList<>());
        }
        
        // 处理显示设备选项
        String testDeviceStr = (String) rawData.get("testDevice");
        if (testDeviceStr != null && !testDeviceStr.isEmpty()) {
            List<String> testDeviceList = java.util.Arrays.asList(testDeviceStr.split(","));
            // 将"空值"转换为空字符串，用于前端显示和筛选
            testDeviceList = testDeviceList.stream()
                .map(value -> "空值".equals(value) ? "" : value)
                .collect(java.util.stream.Collectors.toList());
            result.put("testDevice", testDeviceList);
        } else {
            result.put("testDevice", new java.util.ArrayList<>());
        }
        
        // 处理DQE负责人选项
        String dqeStr = (String) rawData.get("dqe");
        if (dqeStr != null && !dqeStr.isEmpty()) {
            List<String> dqeList = java.util.Arrays.asList(dqeStr.split(","));
            // 将"空值"转换为空字符串，用于前端显示和筛选
            dqeList = dqeList.stream()
                .map(value -> "空值".equals(value) ? "" : value)
                .collect(java.util.stream.Collectors.toList());
            result.put("dqe", dqeList);
        } else {
            result.put("dqe", new java.util.ArrayList<>());
        }
        
        return result;
    }
    
    /**
     * 保存搜索历史
     * @param username 用户名
     * @param filters 搜索条件（不再保存，仅用于生成描述）
     * @param filterDescription 搜索条件描述
     * @param pageName 保存的页面名称
     * @return 是否保存成功
     */
    public boolean saveSearchHistory(String username, Map<String, Object> filters, String filterDescription, String pageName) {
        try {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUsername(username);
            searchHistory.setFilterDescription(filterDescription);
            searchHistory.setPageName(pageName);
            searchHistory.setCreatedAt(LocalDateTime.now());
            
            // 不再保存filters字段到数据库
            
            int result = searchHistoryDao.saveSearchHistory(searchHistory);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 根据用户名获取搜索历史
     * @param username 用户名
     * @param limit 限制数量（可选）
     * @return 搜索历史列表
     */
    public List<SearchHistory> getSearchHistoryByUser(String username, Integer limit) {
        return searchHistoryDao.getSearchHistoryByUser(username, limit);
    }
    
    /**
     * 删除搜索历史
     * @param id 搜索历史ID
     * @return 是否删除成功
     */
    public boolean deleteSearchHistory(Long id) {
        int result = searchHistoryDao.deleteSearchHistory(id);
        return result > 0;
    }
    
    /**
     * 删除用户的所有搜索历史（如果username为null或空，则删除所有用户的历史）
     * @param username 用户名（可选，如果为null或空则删除所有用户的历史）
     * @return 是否删除成功
     */
    public boolean deleteAllByUser(String username) {
        int result = searchHistoryDao.deleteAllByUser(username);
        return result >= 0;
    }
    
    /**
     * 从搜索历史中恢复搜索条件
     * @param id 搜索历史ID
     * @return 搜索条件Map（由于不再保存filters，返回空Map）
     */
    public Map<String, Object> getFiltersFromHistory(Long id) {
        // 由于不再保存filters字段，无法恢复完整的搜索条件
        // 返回空Map，前端可以提示用户手动设置搜索条件
        return new HashMap<>();
    }
    
    /**
     * 根据ID获取搜索历史
     * @param id 搜索历史ID
     * @return 搜索历史对象
     */
    public SearchHistory getSearchHistoryById(Long id) {
        return searchHistoryDao.getSearchHistoryById(id);
    }
}
