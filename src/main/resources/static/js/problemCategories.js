// 问题类别配置文件
// 直接配置为"一级-二级-三级"格式，方便维护

const problemCategories = [
    // 视频组问题类别
    "视频组-视频播放-播放卡顿",
    "视频组-视频播放-播放失败", 
    "视频组-视频播放-播放延迟",
    "视频组-视频播放-播放画质问题",
    "视频组-视频录制-录制失败",
    "视频组-视频录制-录制质量差",
    "视频组-视频录制-录制中断",
    "视频组-视频录制-录制格式问题",
    "视频组-视频编码-编码错误",
    "视频组-视频编码-编码速度慢",
    "视频组-视频编码-编码质量差",
    "视频组-视频编码-编码格式不支持",
    "视频组-视频解码-解码失败",
    "视频组-视频解码-解码错误",
    "视频组-视频解码-解码速度慢",
    "视频组-视频解码-解码画质差",
    "视频组-视频传输-传输中断",
    "视频组-视频传输-传输延迟",
    "视频组-视频传输-传输失败",
    "视频组-视频传输-传输质量差",
    
    // 蓝牙组问题类别
    "蓝牙组-蓝牙连接-连接失败",
    "蓝牙组-蓝牙连接-连接不稳定",
    "蓝牙组-蓝牙连接-连接超时",
    "蓝牙组-蓝牙连接-连接中断",
    "蓝牙组-蓝牙配对-配对失败",
    "蓝牙组-蓝牙配对-配对超时",
    "蓝牙组-蓝牙配对-配对错误",
    "蓝牙组-蓝牙配对-配对不稳定",
    "蓝牙组-蓝牙音频-音频断断续续",
    "蓝牙组-蓝牙音频-音频延迟",
    "蓝牙组-蓝牙音频-音频质量差",
    "蓝牙组-蓝牙音频-音频中断",
    "蓝牙组-蓝牙信号-信号弱",
    "蓝牙组-蓝牙信号-信号不稳定",
    "蓝牙组-蓝牙信号-信号中断",
    "蓝牙组-蓝牙信号-信号干扰",
    "蓝牙组-蓝牙兼容性-设备不兼容",
    "蓝牙组-蓝牙兼容性-协议不兼容",
    "蓝牙组-蓝牙兼容性-版本不兼容",
    "蓝牙组-蓝牙兼容性-功能不兼容",
    
    // 硬件组问题类别
    "硬件组-硬件设计-电路设计问题",
    "硬件组-硬件设计-PCB设计问题",
    "硬件组-硬件设计-机械设计问题",
    "硬件组-硬件设计-散热设计问题",
    "硬件组-生产问题-生产工艺问题",
    "硬件组-生产问题-装配问题",
    "硬件组-生产问题-测试问题",
    "硬件组-生产问题-质量控制问题",
    "硬件组-器件问题-器件损坏",
    "硬件组-器件问题-器件不匹配",
    "硬件组-器件问题-器件性能差",
    "硬件组-器件问题-器件兼容性问题",
    "硬件组-接口问题-接口损坏",
    "硬件组-接口问题-接口不匹配",
    "硬件组-接口问题-接口信号问题",
    "硬件组-接口问题-接口兼容性问题",
    "硬件组-外观问题-外观损坏",
    "硬件组-外观问题-外观缺陷",
    "硬件组-外观问题-外观不匹配",
    "硬件组-外观问题-外观质量问题",
    
    // 软件组问题类别
    "软件组-系统问题-系统崩溃",
    "软件组-系统问题-系统卡顿",
    "软件组-系统问题-系统启动失败",
    "软件组-系统问题-系统兼容性问题",
    "软件组-驱动问题-驱动安装失败",
    "软件组-驱动问题-驱动不兼容",
    "软件组-驱动问题-驱动冲突",
    "软件组-驱动问题-驱动性能问题",
    "软件组-固件问题-固件升级失败",
    "软件组-固件问题-固件损坏",
    "软件组-固件问题-固件不兼容",
    "软件组-固件问题-固件性能问题",
    "软件组-应用问题-应用崩溃",
    "软件组-应用问题-应用卡顿",
    "软件组-应用问题-应用功能异常",
    "软件组-应用问题-应用兼容性问题",
    "软件组-界面问题-界面显示异常",
    "软件组-界面问题-界面卡顿",
    "软件组-界面问题-界面操作异常",
    "软件组-界面问题-界面兼容性问题"
];

// 获取所有问题类别的函数
function getAllProblemCategories() {
    return problemCategories;
}

// 根据组别获取问题类别
function getCategoriesByGroup(groupName) {
    return problemCategories.filter(item => item.startsWith(groupName + '-'));
}

// 获取所有组别信息
function getAllGroups() {
    const groups = new Set();
    problemCategories.forEach(item => {
        const parts = item.split('-');
        if (parts.length >= 1) {
            groups.add(parts[0]);
        }
    });
    return Array.from(groups).map(groupName => ({
        key: groupName,
        name: groupName
    }));
}

// 获取一级分类选项
function getLevel1Categories() {
    const level1Set = new Set();
    problemCategories.forEach(item => {
        const parts = item.split('-');
        if (parts.length >= 1) {
            level1Set.add(parts[0]);
        }
    });
    return Array.from(level1Set).map(item => ({
        value: item,
        text: item
    }));
}

// 根据一级分类获取二级分类选项
function getLevel2Categories(level1) {
    const level2Set = new Set();
    problemCategories.forEach(item => {
        if (item.startsWith(level1 + '-')) {
            const parts = item.split('-');
            if (parts.length >= 2) {
                level2Set.add(parts[1]);
            }
        }
    });
    return Array.from(level2Set).map(item => ({
        value: item,
        text: item
    }));
}

// 根据一级和二级分类获取三级分类选项
function getLevel3Categories(level1, level2) {
    const level3Set = new Set();
    const prefix = level1 + '-' + level2 + '-';
    problemCategories.forEach(item => {
        if (item.startsWith(prefix)) {
            const parts = item.split('-');
            if (parts.length >= 3) {
                level3Set.add(parts[2]);
            }
        }
    });
    return Array.from(level3Set).map(item => ({
        value: item,
        text: item
    }));
}

// 根据选择的级别生成搜索关键字
function generateSearchKeyword(level1, level2, level3) {
    let keyword = '';
    if (level1) {
        keyword = level1;
        if (level2) {
            keyword += '-' + level2;
            if (level3) {
                keyword += '-' + level3;
            }
        }
    }
    return keyword;
}

// 生成问题类别下拉框HTML（三级结构）
function generateProblemCategorySelect(selectedValue = '') {
    let html = '<select id="problemCategorySelect" class="detail-input">';
    html += '<option value="">请选择问题类别</option>';
    
    // 按组别分组
    const groups = {};
    problemCategories.forEach(item => {
        const parts = item.split('-');
        if (parts.length >= 3) {
            const groupName = parts[0];
            const level2 = parts[1];
            const level3 = parts[2];
            
            if (!groups[groupName]) {
                groups[groupName] = {};
            }
            if (!groups[groupName][level2]) {
                groups[groupName][level2] = [];
            }
            groups[groupName][level2].push(level3);
        }
    });
    
    Object.keys(groups).forEach(groupName => {
        html += `<optgroup label="${groupName}">`;
        Object.keys(groups[groupName]).forEach(level2 => {
            groups[groupName][level2].forEach(level3 => {
                const fullValue = `${groupName}-${level2}-${level3}`;
                const selected = fullValue === selectedValue ? 'selected' : '';
                html += `<option value="${fullValue}" ${selected}>${level2}-${level3}</option>`;
            });
        });
        html += '</optgroup>';
    });
    
    html += '</select>';
    return html;
}

// 生成筛选用的问题类别下拉框HTML（三级结构）
function generateFilterProblemCategorySelect(selectedValue = '') {
    let html = '<select id="problemCategoryFilter">';
    html += '<option value="">全部</option>';
    
    // 按组别分组
    const groups = {};
    problemCategories.forEach(item => {
        const parts = item.split('-');
        if (parts.length >= 3) {
            const groupName = parts[0];
            const level2 = parts[1];
            const level3 = parts[2];
            
            if (!groups[groupName]) {
                groups[groupName] = {};
            }
            if (!groups[groupName][level2]) {
                groups[groupName][level2] = [];
            }
            groups[groupName][level2].push(level3);
        }
    });
    
    Object.keys(groups).forEach(groupName => {
        html += `<optgroup label="${groupName}">`;
        Object.keys(groups[groupName]).forEach(level2 => {
            groups[groupName][level2].forEach(level3 => {
                const fullValue = `${groupName}-${level2}-${level3}`;
                const selected = fullValue === selectedValue ? 'selected' : '';
                html += `<option value="${fullValue}" ${selected}>${level2}-${level3}</option>`;
            });
        });
        html += '</optgroup>';
    });
    
    html += '</select>';
    return html;
}

// 生成三级筛选下拉框HTML
function generateThreeLevelFilterSelects() {
    let html = '';
    
    // 一级下拉框
    html += '<select id="problemCategoryLevel1Filter">';
    html += '<option value="">全部</option>';
    const level1Options = getLevel1Categories();
    level1Options.forEach(option => {
        html += `<option value="${option.value}">${option.text}</option>`;
    });
    html += '</select>';
    
    // 二级下拉框
    html += '<select id="problemCategoryLevel2Filter">';
    html += '<option value="">全部</option>';
    html += '</select>';
    
    // 三级下拉框
    html += '<select id="problemCategoryLevel3Filter">';
    html += '<option value="">全部</option>';
    html += '</select>';
    
    return html;
}



