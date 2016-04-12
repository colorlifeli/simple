
var dg_toolBar_all = [
{
	text: '筛选',
	align: 'left',
	iconCls: 'icon-up',
	handler: function() {
		return null;
	}
}, {
	text: '打包',
	align: 'left',
	iconCls: 'icon-down',
	handler: function() {
		return null;
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
//	sortable : true,
	//sorter : commonSortor
}, {
	field : 'name',
	title : '名称',
	align : 'center',
//	sortable : true,
//	sorter : function(a, b) {
//		return a.localeCompare(b);
//	},
//	formatter : function(value, row, index) {
//			
//		return value;
//	}
}, {
	field : 'name',
	title : '名称',
	align : 'center',
}

]];



$(function(){
	$('#bar').dialog('close');
//	$('#allTable').datagrid({  
//        width: 'auto',  
//        height:300,               
//        striped: true,  
//        singleSelect : true,  
//        url:basePath + "stock/computeAll",  
//        //queryParams:{},  
//        loadMsg:'数据加载中请稍后……',  
//        pagination: true,  
//        rownumbers: true,     
//        columns:dg_columns_all,
//        toolbar: dg_toolBar_all
//    });
	
//	$("#allTable").datagrid({
//		url : basePath + "stock/computeAll",
//		width: 'auto',
//		height:300,
//		striped: true,
//		title : 'abc',
//        pagination : true,
//        pageSize : 10,
//        pageList : [ 10, 20, 30, 40 ],
//        columns: dg_columns_all,
//		toolbar: dg_toolBar_all
//	});
	
});


function compute(){
	
	var url = basePath + "stock/computeAll";
	
	$('#bar').dialog('open');
	$.ajax({
		async: "false",
		tyep : "POST",
		url : url,
		context : document.body,
		success : function(data) {
			$('#bar').dialog('close');
			var result = eval("("+data+")");
			$.messager.alert('提示',result.msg,'info');
		}
	});
}

function getOperSum(){

	var url = basePath + "stock/getOperSumAll";

	$("#allTable").datagrid({
		url : url,
		title : '结果',
        pagination : true,
        pageSize : 2,
        pageList : [ 2,20,30,50,100 ],
        columns: dg_columns_all,
		toolbar: dg_toolBar_all
	});
}