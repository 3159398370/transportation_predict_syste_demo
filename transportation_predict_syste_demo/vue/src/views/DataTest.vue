<template>
    <el-upload
            :action="'http://' + serverIp + ':9090/DataTest/upload'"
            :on-success="onSuccess"
            class="upload-demo"
            drag
            multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将预测集拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">请上传预测集</div>
    </el-upload>
</template>

<script>
    import {serverIp} from "../../public/config";
    export default {
        name: "update",
        data() {
            return {
                serverIp: serverIp,
            }
        },
        methods: {
            onSuccess(){
                //this.$router.push('/dashbord');
              this.$router.push('/TestFile');
            },
        }
    }
</script>

<style scoped>

</style>
