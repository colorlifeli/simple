<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/common/head.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>k</title>
</head>
<body>

<div id="main" style="height:600px"></div>
    <!-- ECharts单文件引入 -->
    <script src="<%=ctx %>/js/echarts-all.js"></script>
    
    
    <script type="text/javascript">
        // 基于准备好的dom，初始化echarts图表
        var myChart = echarts.init(document.getElementById('main')); 
        
        option = {
        	    title : {
        	        text: '<%=request.getAttribute("name")%>'
        	    },
        	    tooltip : {
        	        trigger: 'axis',
        	        formatter: function (params) {
        	            var res = params[0].seriesName + ' ' + params[0].name;
        	            res += '<br/>  开盘 : ' + params[0].value[0] + '  最高 : ' + params[0].value[3];
        	            res += '<br/>  收盘 : ' + params[0].value[1] + '  最低 : ' + params[0].value[2];
        	            return res;
        	        }
        	    },
        	    legend: {
        	        data:['<%=request.getAttribute("name")%>']
        	    },
        	    toolbox: {
        	        show : true,
        	        feature : {
        	            mark : {show: true},
        	            dataZoom : {show: true},
        	            dataView : {show: true, readOnly: false},
        	            magicType: {show: true, type: ['line', 'bar']},
        	            restore : {show: true},
        	            saveAsImage : {show: true}
        	        }
        	    },
        	    dataZoom : {
        	        show : true,
        	        realtime: true,
        	        start : 50,
        	        end : 100
        	    },
        	    xAxis : [
        	        {
        	            type : 'category',
        	            boundaryGap : true,
        	            axisTick: {onGap:false},
        	            splitLine: {show:false},
        	            data : [
        	                    <%=request.getAttribute("dates")%>
        	            ]
        	        }
        	    ],
        	    yAxis : [
        	        {
        	            type : 'value',
        	            scale:true,
        	            boundaryGap: [0.01, 0.01]
        	        }
        	    ],
        	    series : [
        	        {
        	            name:'<%=request.getAttribute("name")%>',
        	            type:'k',
        	            data:[ // 开盘，收盘，最低，最高
        	                   <%=request.getAttribute("prices")%>
        	                  
        	            ]
        	        }
        	    ]
        	};

        // 为echarts对象加载数据 
        myChart.setOption(option); 
    </script>
</body>
</html>