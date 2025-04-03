<template>
  <div style="color: #666;font-size: 14px;">
    <div style="padding-bottom: 20px">
     <h1 style="text-align: center"><b>亲爱的{{ user.nickname }}，欢迎登录AI多模型的交通情况预测系统</b></h1>
    </div>
      <el-row :gutter="10" style="margin-bottom: 5px">
        <el-col :span="6">
          <el-card  style="color: #409EFF" >
            <div><i class="el-icon-user-solid" />当前用户总数</div>
            <div style="padding: 10px 0; text-align: center; font-weight: bold" v-text="total">
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card style="color: #409EFF;margin-bottom:50px">
            <div><i class="el-icon-user-solid" />拥堵总数</div>
            <div style="padding: 10px 0; text-align: center; font-weight: bold" v-text="total2">
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card style="color: #409EFF;margin-bottom:50px">
            <div><i class="el-icon-user-solid" />今日拥堵预测总数</div>
            <div style="padding: 10px 0; text-align: center; font-weight: bold" v-text="total1">
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card style="color: #409EFF;margin-bottom:50px">
            <div><i class="el-icon-user-solid" />当前重度拥堵情况</div>
            <div style="padding: 10px 0; text-align: center; font-weight: bold" ><!--v-text="total3"-->
              {{total3}}
            </div>
          </el-card>
        </el-col>
   </el-row>
      <el-row :gutter="20">
        <el-col :span="12" >
          <el-card  shadow="hover" style="width: 100%;">
            <div slot="header" class="clearfix">
              <span>交通拥堵分类图</span>
            </div>
            <el-col :span="12">
              <div id="tu" style="width: 600px; height: 600px"></div>
            </el-col>
          </el-card>
        </el-col>
        <el-col :span="12" >
          <el-card  shadow="hover" style="width: 100%;">
            <div slot="header" class="clearfix">
              <span>每日上传测试集数</span>
            </div>
            <el-col :span="12">
              <div id="tu1" style="width: 600px; height: 600px"></div>
            </el-col>
          </el-card>
        </el-col>
      </el-row>

  </div>
</template>
<script>
import * as echarts from 'echarts'

export default {
  name: "Home",
  data() {
    return {
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      total: 0,
      total1: 0,
      total2: 0,
      total3:0,

    }

  },
  created() {
        this.load(),
        this.load1(),
        this.load2(),
        this.load3()
  },
  methods: {
    load() {
      this.request.get("/user/totle", {
      }).then(res => {
        this.total = res.data
      })
    },
    load2() {
      this.request.get("/echarts/totle").then(res => {
        this.total2=res.data
      })
    },
    load1() {
      this.request.get("/echarts/totle1", {
      }).then(res => {
        this.total1 = res.data
      })
    },
    load3() {
      this.request.get("/echarts/totle3").then(res => {
        this.total3=res.data
      })
    },
  },
  mounted(){


    var chartDom = document.getElementById('tu');
    var myChart = echarts.init(chartDom);
    var option;
    option = {
      title: [
      ],
      polar: {
        radius: [10, '85%']
      },
      angleAxis: {
        max:function (value) {
          return value.max+500;
        },
        startAngle: 75
      },
      radiusAxis: {
        type: 'category',
        data: ['正常', '轻度拥堵', '重度拥堵']
      },
      tooltip: {},
      series: {
        type: 'bar',
        data: [],
        coordinateSystem: 'polar',
        label: {
          show: true,
          position: 'middle',
          formatter: '{b}: {c}'
        },
        itemStyle: {
          normal: {
            color(params) {
              const colorList = ['#5470C6', '#91CC75', '#FAC858', '#EE6666','#73C0DE','#3BA272'];
              return colorList[params.dataIndex];
            }
          }
        }
      }
    };

    var chartDomdis = document.getElementById('tu1');
    var myChartdis = echarts.init(chartDomdis);
    var optiondis;
    optiondis = {
      xAxis: {
        type: 'category',
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: [],
          type: 'line'
        }
      ]
    };
    this.request.get("/echarts/members").then(res => {
      // 填空
      // 数据准备完毕之后再set
      option.series.data = res.data;
      //let max1=Math.max.apply(null,res.data);
      myChart.setOption(option)
    }),
    this.request.get("/home/members").then(res => {
      // 填空
      // 数据准备完毕之后再set
      optiondis.series[0].data = res.data
      myChartdis.setOption(optiondis)
    })
  }
}
</script>
