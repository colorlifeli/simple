
var dg_columns_tableNames = [ [ 
{
	field : 'tableName',
	title : '表',
	align : 'left',
	width : 200,
	formatter : function(value, row, index) {
		var htmlContent = 
			  '<a style="cursor:pointer;text-decoration: underline;" href="javascript:void(0)" ' 
			+ ' onclick="showTable(\'' + value + '\')">' + value + '</a>';
		
		return htmlContent;

	}
}
]];

$(function() {
	var url = basePath + "db/getTableNamesAll";
	
	$("#allTable").datagrid({
		url : url,
		title : '',
        pagination : true,
        singleSelect:true,
        pageSize : 20,
        pageNumber : 1,  //每次按查询时重置为页面为 1。否则第一次结果有10，正在查看第10页，第二次查询的结果只有1页，但发送到后台的page数值仍为10，会导致出错
        pageList : [ 2,20,30,50,100 ],
        columns: dg_columns_tableNames
		//toolbar: dg_toolBar_all
	});
	
	$("#data").datagrid({
		title : '',
        pagination : true,
        singleSelect:true,
        pageSize : 20,
        pageNumber : 1,
        pageList : [ 2,20,30,50,100 ],

		//toolbar: dg_toolBar_all
	});
	$('#data').datagrid({ columns:[[]], rownumbers:false,pagination:false  });
	
	//实时监控表名的变化
//	$('#pattern').bind('input propertychange', function() {
//		var pattern = $('#pattern').combobox('getValue');
//		var url = basePath + "db/getTableNames?pattern=" + pattern;
//		$('#pattern').combobox('reload', url);
//	});
	
	$('#pattern').combobox({
		onChange: function(n,o) {
			getTableNamesCombobox();
		}
//		keyHandler: {
//			up: getTableNamesCombobox(),
//			down:getTableNamesCombobox(),
//			enter:getTableNamesCombobox(),
//			query: function(q){getTableNamesCombobox();}
//		}
	});
});

function getTableNamesCombobox() {
	var pattern = $('#pattern').combobox('getValue');
	var url = basePath + "db/getTableNames?pattern=" + pattern;
	
	$.ajax({
		url : url,
		type : 'post',
		//data : params,
		dataType : "json",
		async : true,
		success : function(val) {
			var result = eval(val);
			var data = result.rows;
			$('#pattern').combobox('loadData', data);
		}
	});
	//$('#pattern').combobox('reload', url);
}

function getTableNames() {
	//var pattern = $("#pattern").val();
	var pattern = $('#pattern').combobox('getValue');
	var url = basePath + "db/getTableNames?pattern=" + pattern;
	
	$('#allTable').datagrid('options').url = url;
	$('#allTable').datagrid('load');
}


var types;
function showTable(tableName) {

	//清空查询条件表格
	$('#conditionTable tr').remove();
	$('#tableName').val(tableName);
	
	getData('tableName=' + tableName, true);

}

function queryTable() {
	
	//var data = $("#formCondition").serialize();
	
	var data = $("#formCondition input");
	var str = '';
	for(var i=0; i<data.length; i++) {
		var item = data[i];
		if(i != 0)
			str = str + "&";
		str = str + item.id + "=" + item.value + "," + types[item.id];
	}
	var tableName = $('#tableName').val();
	str = str + "&tableName=" + tableName;
	
	getData(str, false);
}

function getData(params, isAddCondition) {

	//开始时隐藏 datagrid
	$('#data').datagrid({ url:'', columns:[[]], pagination:false  });
	
	var url = basePath + "db/getData";
	var url_count = basePath + "db/getCount";
	
	var columns = new Array();
	var $datagrid = {};
	
	var realTotal = 0;

	$.ajax({
		url : url,
		type : 'post',
		data : params,
		dataType : "json",
		async : false,
		success : function(val) {
			var result = eval(val);
			var total = result.total;
			// 根据返回的数据动态生成列。列的名称由第一行数据的各个属性的名字（即key)来确定。
			if(total > 0) {
				var row = result.rows[0];
				types = result.types;
				$.each(row, function(key, value) {
					columns.push({
						"field" : key,
						"title" : key,
						"width" : 100,
						"sortable" : true
					});

					//增加查询条件
					if(isAddCondition)
						$('#conditionTable').append(buildTr(key, types[key]));
				});
				
				$datagrid.columns = new Array(columns);
				$datagrid.data = result.rows;
				$datagrid.pagination = true;
				$('#data').datagrid($datagrid);
				
				//刷新总数
				var pager = $('#data').datagrid('getPager');
				if(realTotal > 0) {
					pager.pagination('refresh',{total:realTotal});
				} else {
					pager.pagination({
						displayMsg : '后台数据正在加载...请稍候...',
						afterPageText : '页'
					});
				}
				//赋url以便点击“下一页”时能向后台查询数据
				$('#data').datagrid('options').url = url;
			}
		}
	});

	//获取数量
	$.ajax({
		url : url_count,
		type : 'post',
		data : params,
		dataType : "json",
		async : true,
		success : function(val) {
			var result = eval(val);
			realTotal = result.total;
			if(realTotal > 0) {
				var pager = $('#data').datagrid('getPager');
				pager.pagination('refresh',{total:realTotal});
				pager.pagination({
					displayMsg : '显示 {from} 到 {to} 共 {total} 记录',
					afterPageText : '共{pages}页'
				});
			}
		}
	});
	
}

function buildTr(key, type) {
	var html = '<tr><td class="bgc_tt short">' + key +
		'</td><td class="long"><input name="' + key + '" id="' + key + '" type="text" size="10">' + 
		'(' + type + ')' +
		'</td></tr>';
	return html;
}