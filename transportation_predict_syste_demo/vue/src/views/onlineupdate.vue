<template>
    <el-upload
            :action="'http://' + serverIp + ':9090/python/upload'"
            :on-success="onSuccess"
            class="upload-demo"
            drag
            multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将训练集拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">请上传训练集</div>
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
                this.$router.push('/file');
            },
        }
    }
</script>

<style scoped>

</style>
