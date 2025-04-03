<template>
  <div class="app-container">
    <div style="margin: 10px 0">
      <label>发送者姓名：</label>
      <el-input style="width: 200px" placeholder="请输入发送者姓名" suffix-icon="el-icon-search" v-model="sendUserName"></el-input>
      <el-button class="ml-5" type="primary" @click="load">查询</el-button>
      <el-button type="warning" @click="reset">重置</el-button>
    </div>
    <el-table :data="tableData" border stripe :header-cell-class-name="'headerBg'"
              @selection-change="handleSelectionChange"
    >
      <!-- <el-table-column type="selection" width="55"></el-table-column>-->
      <el-table-column prop="id" label="Id"  width="100" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip/>
      <el-table-column prop="type" label="交通情况类型" show-overflow-tooltip/>
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="sendUserName" label="发送人"  width="100" />
      <el-table-column prop="receiveUserName" label="接收人"  show-overflow-tooltip />
      <!--<el-table-column prop="readCount" label="已读数" width="70" />
      <el-table-column prop="receiveUserCount" label="接收人数" width="100" />-->
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
  name: "List",
  data () {
    return {
      serverIp: serverIp,
      tableData: [],
      sendUserName: '',
      multipleSelection: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
    }
  },
  created() {
    this.load()
  },
  methods:{
    load() {
      this.request.get("/message/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          sendUserName: this.sendUserName,
        }
      }).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
      })
    },
    handleSelectionChange(val) {
      console.log(val)
      this.multipleSelection = val
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
    reset() {
      this.sendUserName = ""
      this.load()
    },
  }
}
</script>

<style scoped>

</style>
