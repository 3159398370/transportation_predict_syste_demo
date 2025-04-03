<template>
    <div >
        <div style="margin: 10px 0">
            <el-input style="width: 200px" placeholder="请输入交通类型" suffix-icon="el-icon-search" v-model="result"></el-input>
            <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
            <el-button type="warning" @click="reset">重置</el-button>
        </div>

        <el-table :data="tableData" border stripe :header-cell-class-name="'headerBg'"
                  @selection-change="handleSelectionChange"
        >
           <!-- <el-table-column type="selection" width="55"></el-table-column>-->
            <el-table-column prop="testid" label="测试ID" width="100"></el-table-column>
            <el-table-column prop="result" label="交通情况类型" ></el-table-column>
            <el-table-column prop="createTime" label="生成时间" ></el-table-column>

            <el-table-column label="操作" width="300" align="center">
              <template slot-scope="scope" >
                <el-button type="primary" @click="goSend()" >调度策略下发
                </el-button>
              </template>
            </el-table-column>

        </el-table>

        <div style="padding: 10px 0">
            <el-pagination
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page="pageNum"
                    :page-sizes="[2, 5, 10, 20]"
                    :page-size="pageSize"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total">
            </el-pagination>
        </div>

    </div>
</template>

<script>
    import {serverIp} from "../../public/config";

    export default {
        name: "Detilebord",
        data() {
            return {
                serverIp: serverIp,
                tableData: [],
                result: '',
                multipleSelection: [],
                pageNum: 1,
                pageSize: 10,
                total: 0,
                id:this.$route.query.id1
            }
        },
        created() {
            this.load()
        },
        methods: {
            load() {
              if (typeof(this.id) == "undefined"){
                this.request.get("/detilebord/page/", {
                  params: {
                    pageNum: this.pageNum,
                    pageSize: this.pageSize,
                    result:this.result,
                  }
                }).then(res => {
                  this.tableData = res.data.records
                  this.total = res.data.total
                })
              }else {
                this.request.get("/detilebord/page/"+this.id, {
                  params: {
                    pageNum: this.pageNum,
                    pageSize: this.pageSize,
                    result:this.result,
                  }
                }).then(res => {
                  this.tableData = res.data.records
                  this.total = res.data.total
                })
              }
            },

            handleSelectionChange(val) {
                console.log(val)
                this.multipleSelection = val
            },
            reset() {
                this.result = ""
                this.load()
            },
            handleSizeChange(pageSize) {
                console.log(pageSize)
                this.pageSize = pageSize
                this.load()
            },
            handleCurrentChange(pageNum) {
                console.log(pageNum)
                this.pageNum = pageNum
                this.load()
            },
          goSend(){
            this.$router.push('/Send');
          }
        }
    }
</script>

<style scoped>

</style>
