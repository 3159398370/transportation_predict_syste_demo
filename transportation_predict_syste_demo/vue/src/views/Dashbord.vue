<template>
    <div>
        <el-row :gutter="10" style="margin-bottom: 60px">
            <el-col :span="6">
                <el-card style="color: #409EFF">
                    <div><i class="el-icon-user-solid"/> 交通预测总数</div>
                    <div style="padding: 10px 0; text-align: center; font-weight: bold" v-text="totle">
                    </div>
                  <div style="padding: 10px 0; text-align: right; font-weight: bold"> <el-button type="text" @click="show()">详情明细</el-button></div>

                </el-card>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="12">
                <div id="main" style="width: 500px; height: 400px"></div>
            </el-col>

            <el-col :span="12">
                <div id="pie" style="width: 500px; height: 400px"></div>
            </el-col>
        </el-row>

    </div>
</template>

<script>
    import * as echarts from 'echarts';
    export default {
        name: "Dashbord",
        data() {
            return {
                totle:0,
                id:this.$route.query.id
            }
        },
        mounted() {  // 页面元素渲染之后再触发
            var chartDom = document.getElementById('main');
            var myChart = echarts.init(chartDom);
            var option;
            option = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                grid: {
                    left: '3%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis: [
                    {
                        type: 'category',
                        data: ['正常', '轻度拥堵', '重度拥堵'],
                        axisTick: {
                            alignWithLabel: true
                        }
                    }
                ],
                yAxis: [
                    {
                        type: 'value'
                    }
                ],
                series: [
                    {
                        name: '交通状况数量',
                        type: 'bar',
                        barWidth: '60%',
                        data: [],
                        itemStyle: {
                        normal: {
                          color(params) {
                            const colorList = ['#5470C6', '#91CC75', '#FAC858', '#EE6666','#73C0DE','#3BA272'];
                            return colorList[params.dataIndex];
                          }
                        }
                      }
                    }
                ],
                toolbox: {
                    show: true,
                    left: 'center',
                    feature: {
                        mark: {show: true},
                        saveAsImage: {
                            show: true,
                            title : '下载图片',
                        },
                    }
                },
            };
            myChart.setOption(option);
            // 饼图
            var chartDomPie = document.getElementById('pie');
            var myPieChart = echarts.init(chartDomPie);
            var optionPie;
            optionPie = {
                title: {
                    text: '交通情况分类占比',
                    subtext: '比列图',
                    left: 'center'
                },
                tooltip: {
                    trigger: 'item'
                },
                legend: {
                    orient: 'vertical',
                    left: 'left'
                },
                series: [
                    {
                        type: 'pie',
                        radius: '50%',
                        data: [],
                        emphasis: {
                            itemStyle: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
                        }
                    }
                ],
                toolbox: {
                    show: true,
                    left: 'right',
                    feature: {
                        mark: {show: true},
                        saveAsImage: {
                            show: true,
                            title : '下载',
                        },
                    }
                },
            };
            if (typeof(this.id) == "undefined"){
              this.request.get("/echarts/members").then(res => {
                // 填空
                option.series[0].data = res.data
                // 数据准备完毕之后再set
                myChart.setOption(option);
                optionPie.series[0].data = [
                  {name: "正常", value: res.data[0]},
                  {name: "轻度拥堵", value: res.data[1]},
                  {name: "重度拥堵", value: res.data[2]},

                ]
                myPieChart.setOption(optionPie)
              }),
                  this.request.get("/echarts/totle").then(res => {
                    this.totle=res.data
                  })
            }else {
              this.request.get("/DataTest/members/"+this.id).then(res => {
                // 填空
                option.series[0].data = res.data
                // 数据准备完毕之后再set
                myChart.setOption(option);
                optionPie.series[0].data = [
                  {name: "正常", value: res.data[0]},
                  {name: "轻度拥堵", value: res.data[1]},
                  {name: "重度拥堵", value: res.data[2]},

                ]
                myPieChart.setOption(optionPie)
              }),
                  this.request.get("/DataTest/totle/"+this.id).then(res => {
                    this.totle=res.data
                  })
            }


        },
        methods: {
          show(){
            this.$router.push({
              path: "/Detilebord",
              query: {
                id1: this.id
              }
            });
          }
        }
    }
</script>

<style scoped>

</style>
