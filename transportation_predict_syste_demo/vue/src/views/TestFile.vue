<template>
    <div >
        <div style="margin: 10px 0">
            <el-input style="width: 200px" placeholder="请输入名称" suffix-icon="el-icon-search" v-model="name"></el-input>
            <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
            <el-button type="warning" @click="reset">重置</el-button>
        </div>
        <div style="margin: 10px 0">
            <el-upload :action="'http://' + serverIp + ':9090/DataTest/upload'" :show-file-list="false" accept="xlsx"
                       :on-success="handleFileUploadSuccess" style="display: inline-block">
                <el-button type="primary" class="ml-5">上传文件 <i class="el-icon-top"></i></el-button>
            </el-upload>
            <el-popconfirm
                    class="ml-5"
                    confirm-button-text='确定'
                    cancel-button-text='我再想想'
                    icon="el-icon-info"
                    icon-color="red"
                    title="您确定批量删除这些数据吗？"
                    @confirm="delBatch"
            >
                <el-button type="danger" slot="reference">批量删除 <i class="el-icon-remove-outline"></i></el-button>
            </el-popconfirm>

        </div>
        <el-table :data="tableData" border stripe :header-cell-class-name="'headerBg'"
                  @selection-change="handleSelectionChange"
        >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="name" label="文件名称"></el-table-column>
            <el-table-column prop="type" label="文件类型"></el-table-column>
            <el-table-column prop="size" label="文件大小(kb)"></el-table-column>
            <el-table-column label="在线预测">

                <template slot-scope="scope" >
                    <el-button type="primary" @click="star(scope.row.url)" >在线预测
                    </el-button>
                </template>
            </el-table-column>
            <el-table-column label="查看结果">
                <template slot-scope="scope">
                    <el-button type="primary" @click="show(scope.row.id)"
                               :disabled="scope.row.enable==null">查看结果
                    </el-button>
                </template>
            </el-table-column>

          <el-table-column label="下载">
            <template slot-scope="scope">
              <el-button type="primary" @click="download(scope.row.jsonUrl)"
                         :disabled="scope.row.jsonUrl==null">下载
              </el-button>
            </template>
          </el-table-column>

 <!--           <el-table-column label="启用">
                <template slot-scope="scope">
                    <el-switch v-model="scope.row.enable" active-color="#13ce66" inactive-color="#ccc"
                               @change="changeEnable(scope.row)"></el-switch>
                </template>
            </el-table-column>-->
            <el-table-column label="操作" width="200" align="center">
                <template slot-scope="scope">
                    <el-popconfirm
                            class="ml-5"
                            confirm-button-text='确定'
                            cancel-button-text='我再想想'
                            icon="el-icon-info"
                            icon-color="red"
                            title="您确定删除吗？"
                            @confirm="del(scope.row.id)"
                    >
                        <el-button type="danger" slot="reference">删除 <i class="el-icon-remove-outline"></i></el-button>
                    </el-popconfirm>

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
        name: "TestFile",
        data() {
            return {
                serverIp: serverIp,
                tableData: [],
                name: '',
                multipleSelection: [],
                pageNum: 1,
                pageSize: 10,
                total: 0,
            }
        },
        created() {
            this.load()
        },
        methods: {
            load() {
                this.request.get("/DataTest/page", {
                    params: {
                        pageNum: this.pageNum,
                        pageSize: this.pageSize,
                        name: this.name,
                    }
                }).then(res => {
                    this.tableData = res.data.records
                    this.total = res.data.total
                })
            },
            changeEnable(row) {
                this.request.post("/DataTest/update", row).then(res => {
                    if (res.code === '200') {
                        this.$message.success("操作成功")
                    }
                })
            },
            del(id) {
                this.request.delete("/DataTest/" + id).then(res => {
                    if (res.code === '200') {
                        this.$message.success("删除成功")
                        this.load()
                    } else {
                        this.$message.error("删除失败")
                    }
                })
            },
            handleSelectionChange(val) {
                console.log(val)
                this.multipleSelection = val
            },
            delBatch() {
                let ids = this.multipleSelection.map(v => v.id)  // [{}, {}, {}] => [1,2,3]
                this.request.post("/DataTest/del/batch", ids).then(res => {
                    if (res.code === '200') {
                        this.$message.success("批量删除成功")
                        this.load()
                    } else {
                        this.$message.error("批量删除失败")
                    }
                })
            },
            reset() {
                this.name = ""
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
            handleFileUploadSuccess(res) {
                console.log(res)
                if (res.code === '302') {
                    this.$message.error("只能上传excel")
                } else {
                    this.$message.success("上传成功")
                }
                this.load()
            },
          show(id) {
            this.request.get("/DataTest/members/"+id).then(res => {
              if (res.code === '200') {
                this.$router.push({
                  path: "/Dashbord",
                  query: {
                    id: id
                  }
                });

              } else {
                this.$message.error("失败")
              }
            })

            },
            star(url) {
              url = url.slice(31);
              const loading = this.$loading({
                lock: true,
                text: 'Loading',
                spinner: 'el-icon-loading',
                background: 'rgba(0, 0, 0, 0.7)'
              })
              this.$message("在线预测中")

                this.request.get("DataTest/getUrl/" + url).then(res => {
                    if (res.code == '200') {this.$message.success("预测成功，请查看结果！")
                      this.load()

                    } else if (res.code === '505') {
                        this.load()
                        this.$message.success(res.msg)
                    } else {
                        this.load()
                        this.$message.error("预测失败，请重新预测！")
                    }
                }).finally(()=>{
                  loading.close()
                  this.load();
                });
            },
          download(jsonUrl) {
            jsonUrl = "http://localhost:9090/DataTest/" + jsonUrl
            window.open(jsonUrl)
            this.$router.push('/TestFile')
          },
        }
    }
</script>

<style scoped>

</style>
