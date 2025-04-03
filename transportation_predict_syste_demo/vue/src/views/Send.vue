<template>
  <div class="app-container">
    <el-form :model="form" ref="form" label-width="100px" v-loading="formLoading" :rules="rules">
      <el-form-item label="标题："  prop="title" required>
        <el-input v-model="form.title"></el-input>
      </el-form-item>
      <el-form-item label="内容：" prop="content" required>
        <el-input type="textarea" rows="13"  v-model="form.content"></el-input>
      </el-form-item>
      <el-form-item label="交通情况类型：" required>
        <el-select clearable v-model="form.type" placeholder="请选择交通类型" style="width: 100%">
          <el-option v-for="item in types" :key="item.type" :label="item.type" :value="item.type"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="接收人：" required>
        <el-select clearable v-model="form.receiveUserName" placeholder="请选择用户" style="width: 100%">
          <el-option v-for="item in receiveUserNames" :key="item.username" :label="item.username" :value="item.username"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">发送</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: "Send",
  data () {
    return {
      form: {
        title: '',
        content: '',
        receiveUserName:'',
        type:''
      },
      receiveUserNames:[],
      types:[{
        type:'正常',
      },{
        type:'轻度',
      },{
        type:'重度',
      }],
      formLoading: false,
      selectLoading: false,
      options: [],
      rules: {
        title: [
          { required: true, message: '请输入消息标题', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入消息内容', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.load()
  },
  methods:{
    load() {
      this.request.get("/message/getUserName").then(res => {
        this.receiveUserNames = res.data
      })

    },
    resetForm () {
      this.$refs['form'].resetFields()
      this.options = []
      this.form.receiveUserName = []
      this.form.type=[]
    },
    submitForm(){
      this.request.post("/message/send", this.form).then(res => {
        if(res.code === '200') {
          this.$message.success("发送成功")
          this.$router.push('/list');
        } else {
          this.$message.error(res.msg)
        }
      })
    },
  }
}
</script>

<style scoped>

</style>
