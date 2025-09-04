// 问题类别配置文件
// 按组别分类的问题类别配置

const problemCategories = {
    // 视频组问题类别
    video: {
        groupName: "视频组",
        categories: [
            "由配置文件导入，可以自行配置",
            "视频录制问题", 
            "视频编码问题",
            "视频解码问题",
            "视频传输问题",
            "视频显示问题",
            "视频格式问题",
            "视频画质问题",
            "视频同步问题",
            "视频卡顿问题"
        ]
    },
    
    // 蓝牙组问题类别
    bluetooth: {
        groupName: "蓝牙组", 
        categories: [
            "蓝牙连接问题",
            "蓝牙配对问题",
            "蓝牙传输问题",
            "蓝牙音频问题",
            "蓝牙信号问题",
            "蓝牙兼容性问题",
            "蓝牙功耗问题",
            "蓝牙协议问题",
            "蓝牙设备识别问题",
            "蓝牙断连问题"
        ]
    },
    
    // 硬件组问题类别
    hardware: {
        groupName: "硬件组",
        categories: [
            "硬件设计问题",
            "生产问题",
            "器件问题",
            "配件问题",
            "外观问题",
            "PCBA问题",
            "电路问题",
            "接口问题",
            "散热问题",
            "机械结构问题"
        ]
    },
    
    // 软件组问题类别
    software: {
        groupName: "软件组",
        categories: [
            "软件问题",
            "系统问题",
            "驱动问题",
            "固件问题",
            "应用问题",
            "界面问题",
            "性能问题",
            "兼容性问题",
            "稳定性问题",
            "功能缺失问题"
        ]
    }
};

// 获取所有问题类别的函数
function getAllProblemCategories() {
    let allCategories = [];
    Object.values(problemCategories).forEach(group => {
        allCategories = allCategories.concat(group.categories);
    });
    return allCategories;
}

// 根据组别获取问题类别
function getCategoriesByGroup(groupKey) {
    if (problemCategories[groupKey]) {
        return problemCategories[groupKey].categories;
    }
    return [];
}

// 获取所有组别信息
function getAllGroups() {
    return Object.keys(problemCategories).map(key => ({
        key: key,
        name: problemCategories[key].groupName,
        categories: problemCategories[key].categories
    }));
}

// 生成问题类别下拉框HTML
function generateProblemCategorySelect(selectedValue = '') {
    let html = '<select id="problemCategorySelect" class="detail-input">';
    html += '<option value="">请选择问题类别</option>';
    
    Object.keys(problemCategories).forEach(groupKey => {
        const group = problemCategories[groupKey];
        html += `<optgroup label="${group.groupName}">`;
        group.categories.forEach(category => {
            const selected = category === selectedValue ? 'selected' : '';
            html += `<option value="${category}" ${selected}>${category}</option>`;
        });
        html += '</optgroup>';
    });
    
    html += '</select>';
    return html;
}

// 生成筛选用的问题类别下拉框HTML
function generateFilterProblemCategorySelect(selectedValue = '') {
    let html = '<select id="problemCategoryFilter">';
    html += '<option value="">全部</option>';
    
    Object.keys(problemCategories).forEach(groupKey => {
        const group = problemCategories[groupKey];
        html += `<optgroup label="${group.groupName}">`;
        group.categories.forEach(category => {
            const selected = category === selectedValue ? 'selected' : '';
            html += `<option value="${category}" ${selected}>${category}</option>`;
        });
        html += '</optgroup>';
    });
    
    html += '</select>';
    return html;
}



