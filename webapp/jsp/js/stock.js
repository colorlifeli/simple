
var dg_toolBar_all = [
{
	text: '筛选',
	align: 'left',
	iconCls: 'icon-up',
	handler: function() {
		var page = $("form[name='fmTaskfinish'] input[name='page']");
		var currentPage = parseInt(page.val());
		if (currentPage < 2) {
			$.messager.alert("提示", "已经是第一页", "info");
			return;
		}
		page.val(currentPage - 1);
		queryTaskFinish();
	}
}, {
	text: '打包',
	align: 'left',
	iconCls: 'icon-down',
	handler: function() {
		// 判断是否还有更多结果。如果当前结果列表的数量小于结果列表容量，则认为已经没有更多结果。
		var taskQuery2Rows = $("#taskFinishTable").datagrid("getRows");
		var currentRows = taskQuery2Rows.length;
		var rows = $("form[name='fmTaskfinish'] input[name='rows']").val();
		if (currentRows < parseInt(rows)) {
			$.messager.alert("提示", "已经没有更多结果", "info");
			return;
		}

		var page = $("form[name='fmTaskfinish'] input[name='page']");
		var currentPage = parseInt(page.val());
		page.val(currentPage + 1);
		queryTaskFinish();
	}
}];

var dg_columns_all = [ [ 
{
//	field : 'checkBoxNo',
//	checkbox : true
//}, {
	field : 'code',
	title : '代码',
	align : 'center',
	sortable : true,
	sorter : commonSortor
}, {
	field : 'name',
	title : '名称',
	align : 'center',
	sortable : true,
	sorter : function(a, b) {
		return a.localeCompare(b);
	},
	formatter : function(value, row, index) {
		return '<a style="cursor:pointer;text-decoration: underline;" onclick="javascript:showFinishTaskInfo(\''
				+ row["piid"] + '\')">'+value+'</a>';
		return value;
	}
}]];



$(function(){
	$("allTable").datagrid({
		url : 'abc',
		title : 'abc',
        pagination : true,
        pageSize : 10,
        pageList : [ 10, 20, 30, 40 ],
        columns: page_contentColumnHeaders,
		toolbar: page_toolBar_taskFinish,	
	});
});