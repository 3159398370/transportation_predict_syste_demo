<template>
  <div>
    <div style="margin: 10px 0">
      <el-input style="width: 200px" placeholder="请输入名称" suffix-icon="el-icon-search" v-model="name"></el-input>
      <el-button class="ml-5" type="primary" @click="load">搜索</el-button>
      <el-button type="warning" @click="reset">重置</el-button>
    </div>
    <div style="margin: 10px 0">
      <el-upload :action="'http://' + serverIp + ':9090/python/upload'" :show-file-list="false" accept="xlsx"
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
      <el-table-column label="模型训练" width="300">
        <template slot-scope="scope">
          <el-button type="primary" @click="changeModelFNN();star(scope.row.url)">神经网络
          </el-button>
          <el-button type="primary" @click="changeModelDT();star(scope.row.url)">决策树
          </el-button>
          <el-button type="primary" @click="changeModelRF();star(scope.row.url)">随机森林
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="模型评估">
        <template slot-scope="scope">
          <el-button type="primary" @click="analyze(scope.row.url)"
                     >模型评估
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="下载" width="170">
        <template slot-scope="scope">
          <el-button type="primary" @click="download(scope.row.pythonurl)"
                     :disabled="scope.row.pythonurl==null">模型
          </el-button>
          <el-button type="primary" @click="downloadanalysis(scope.row.report)"
                     :disabled="scope.row.report==null">评估报告
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
  name: "File",
  data() {
    return {
      serverIp: serverIp,
      tableData: [],
      name: '',
      multipleSelection: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      model_type: 'modelFNN',
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.request.get("/file/page", {
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
      this.request.post("/file/update", row).then(res => {
        if (res.code === '200') {
          this.$message.success("操作成功")
        }
      })
    },
    del(id) {
      this.request.delete("/file/" + id).then(res => {
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
      this.request.post("/file/del/batch", ids).then(res => {
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
  download(pythonurl) {
    pythonurl = "http://localhost:9090/python/" + pythonurl
    window.open(pythonurl)
    this.$router.push('/File')
  },
  downloadanalysis(report) {
    report = "http://localhost:9090/python/download"
    window.open(report)
    this.$router.push('/File')
  },
  analyze(url) {
    const loading = this.$loading({
      lock: true,
      text: 'Loading',
      spinner: 'el-icon-loading',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    this.$message("模型评估中")
    this.request.get("python/analyze/" + url.slice(29)).then(res => {
      if (res.code === '200') {
        this.load()
        this.$message.success("模型评估成功,请下载！")
      } else if (res.code === '505') {
        this.load()
        this.$message.success(res.msg)
      } else {
        this.load()
        this.$message.error("模型评估失败,请重新训练")
      }
    })
        .finally(()=>{
          loading.close()
          this.load();
        });

  },
  changeModelFNN() {
    this.model_type = 'modelFNN'
  },
  changeModelDT() {
    this.model_type = 'modelDT'
  },
  changeModelRF() {
    this.model_type = 'modelRF'
  },
  star(url) {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    url = url.slice(29) + '/' + this.model_type;
    console.log(url)
    this.$message("模型训练中")
    this.request.get("python/getUrl/" + url).then(res => {
      if (res.code === '200') {
        this.load()
        this.$message.success("训练成功,请下载！")
      } else if (res.code === '505') {
        this.load()
        this.$message.success(res.msg)
      } else {
        this.load()
        this.$message.error("训练失败,请重新训练")
      }
    })
        .finally(()=>{
          loading.close()
          this.load();
        });
  },
}
}
</script>

<style scoped>

</style>
