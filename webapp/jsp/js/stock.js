
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
	field : 'code',
	title : '代码',
	align : 'center',
	width : 60
}, {
	field : 'name',
	title : '名称',
	align : 'center',
	width : 80
}, {
	field : 'buys',
	title : '买入次数',
	align : 'center',
	sortable : true,
	width : 60
}, {
	field : 'sells',
	title : '卖出次数',
	align : 'center',
	sortable : true,
	width : 60
}, {
	field : 'times',
	title : '0次数',
	align : 'center',
	sortable : true,
	width : 40
}, {
	field : 'winTimes',
	title : 'win次数',
	align : 'center',
	sortable : true,
	width : 50
}, {
	field : 'loseTimes',
	title : 'lose次数',
	align : 'center',
	sortable : true,
	width : 50
}, {
	field : 'lastRemain',
	title : '最后余额',
	align : 'center',
	sortable : true,
	width : 60
}, {
	field : 'minRemain',
	title : '最小余额',
	align : 'center',
	sortable : true,
	width : 70
}, {
	field : 'operation',
	title : '操作',
	align : 'center',
	formatter : function(value, row, index) {
		var code = row.code;
		var htmlContent = 
			  '<a style="cursor:pointer;text-decoration: underline;" href="javascript:void(0)" ' 
			+ ' onclick="getOperList(\'' + code + '\')">详情</a>' + '    ' 
		
		return htmlContent;

	}
}

]];


var dg_columns_codeOper = [ [ 
{
	field : 'sn',
	title : '序号',
	align : 'center',
	width : 30
}, {
	field : 'code',
	title : '名称',
	align : 'center',
	hidden : true,
	width : 80
}, {
	field : 'oper',
	title : '操作',
	align : 'center',
	width : 30,
	formatter : function(value, row, index) {
		if(value == "1")
			return "买";
		else(value == "2")
			return "卖";
	}
}, {
	field : 'date_',
	title : '日期',
	align : 'center',
	width : 70
}, {
	field : 'num',
	title : '数量',
	align : 'center',
	width : 30
}, {
	field : 'price',
	title : '单价',
	align : 'center',
	width : 40
}, {
	field : 'sum',
	title : '总价',
	align : 'center',
	width : 50
}, {
	field : 'total',
	title : '现有数量',
	align : 'center',
	width : 60
}, {
	field : 'remain',
	title : '余额',
	align : 'center',
	width : 60
}

]];


$(function(){
	$('#bar').dialog('close');	
});


function compute(){
	
	var data = $("#pform").serialize();
	var url = basePath + "stock/computeAll?" + data;
	
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
			closeWin("pwin");
		}
	});
}

function writeToDB(){
	
	var url = basePath + "stock/writeToDB";
	
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

	var data = $("#fm1").serialize();
	var isFromDB = $('#isFromDB').is(':checked');
	var url = basePath + "stock/getOperSumAll?" + data + "&isFromDB=" + isFromDB;
			
	$("#allTable").datagrid({
		url : url,
		title : '结果',
        pagination : true,
        singleSelect:true,
        pageSize : 20,
        pageNumber : 1,  //每次按查询时重置为页面为 1。否则第一次结果有10，正在查看第10页，第二次查询的结果只有1页，但发送到后台的page数值仍为10，会导致出错
        pageList : [ 2,20,30,50,100 ],
        columns: dg_columns_all,
		//toolbar: dg_toolBar_all
	});
}

function getOperList(code){

	var isFromDB = $('#isFromDB').is(':checked');
	var url = basePath + "stock/getOperList?code=" + code + "&isFromDB=" + isFromDB;

	$("#codeOperTable").datagrid({
		url : url,
		title : code + ' 操作详情',
        pagination : true,
        pageSize : 20,
        pageList : [ 2,20,30,50,100 ],
        columns: dg_columns_codeOper,
		//toolbar: dg_columns_codeOper
	});
	
	//获取 k
	url = basePath + "stock/k?code=" + code;
	var tab = $("#tabs").tabs('getTab', "k");
	$('#tabs').tabs('update', {
		tab: tab,
		options: {
			title: 'k',
			href: url 
		}
	});
		
}

function summary(){
	var isFromDB = $('#isFromDB').is(':checked');
	var url = basePath + "stock/summary?isFromDB=" + isFromDB;
	
	$('#bar').dialog('open');
	$.ajax({
		async: "false",
		tyep : "POST",
		url : url,
		context : document.body,
		success : function(data) {
			$('#bar').dialog('close');
			var result = eval("("+data+")");
			$("#summary").html(result.msg);
		}
	});
}

function sellAll(){
	var isFromDB = $('#isFromDB').is(':checked');
	var url = basePath + "stock/sellAll?isFromDB=" + isFromDB;
	
	$('#bar').dialog('open');
	$.ajax({
		async: "false",
		tyep : "POST",
		url : url,
		context : document.body,
		success : function(data) {
			$('#bar').dialog('close');
			var result = eval("("+data+")");
			$("#sellAll").html(result.msg);
		}
	});
}

function openWin(name) {
	$('#'+name).window('open');
}

function closeWin(name){
	$('#'+name).window('close');
}
