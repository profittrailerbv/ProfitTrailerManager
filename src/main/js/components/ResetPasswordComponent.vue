<template>
    <div class="row form-margin">
        <div class="col d-flex justify-content-center font-weight-bold align-items-center">
            <div class="card">
                <div class="card-body bg-light">
                    <div class="row px-5 pt-3">
                        <div class="col align-self-center">
                            Reset Password
                        </div>
                    </div>
                    <div class="row mt-3 px-5">
                        <div class="col">
                            <input name="systemId" type="text" v-model="input.code"
                                   placeholder="Discord Reset Code" class="mr-2">
                            <a href="#" @click.prevent="sendCode()" class="font-">
                              <font-awesome-icon :icon="['fas','paper-plane']"
                                                 class="text-dark big"></font-awesome-icon>
                            </a>
                        </div>

                    </div>
                  <div class="row px-5">
                    <div class="col">
                      <span v-if="codesuccess" class="text-soft-success">{{codesuccess}}</span>
                      <span v-if="codeerror" class="text-soft-danger">{{codeerror}}</span>
                    </div>
                  </div>
                    <div class="row mt-3 px-5">
                        <div class="col">
                            <input name="password" type="password" v-model="input.password" placeholder="New Password"
                                   class="mr-2">
                        </div>
                    </div>
                    <div class="row mt-3 px-5">
                        <div class="col">
                            <input name="confirmPassword" type="password" v-model="input.confirm"
                                   placeholder="Confirm Password" class="mr-2">
                            <a href="#" @click.prevent="login()" class="font-">
                                <font-awesome-icon :icon="['fas','sign-in-alt']"
                                                   class="text-dark big"></font-awesome-icon>
                            </a>
                        </div>
                    </div>
                    <div class="row px-5 pt-2 pb-5">
                        <div class="col">
                            <span v-if="error" class="text-soft-danger">{{error}}</span>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                error: "",
                codeerror:"",
                codesuccess:"",
                input: {
                    password: "",
                    confirm: "",
                    code: ""
                }
            }
        },
        methods: {
            login() {
                if (this.input.password !== "" && this.input.confirm !== "" && this.input.code !== "") {
                    const formData = new FormData();
                    formData.append("password", encodeURIComponent(this.input.password));
                    formData.append("passwordConfirm", encodeURIComponent(this.input.confirm));
                    formData.append("code", this.input.code);

                    axios.post('/api/v1/resetPassword', formData).then(() => {
                        window.location.href = '/';
                    }).catch((error) => {
                        if (!error.response) {
                            window.location.href = '/';
                        } else {
                            this.error = error.response.data.message;
                        }
                    })
                } else {
                    this.error = "Provide a password"
                }
            },
            sendCode() {
                axios.post('/api/v1/resetPassword/code').then((response) => {
                  console.log(response.data);
                  this.codeerror = response.data.error;
                  this.codesuccess = response.data.success;
                }).catch((error) => {

                  if (error.response) {
                    this.codeerror = error.response.data.message;
                  }
                })
            }
        }
    }
</script>

<style scoped>
    .form-margin {
        margin-top: 20%;
    }

    .text-soft-danger {
        color: #be584a;
    }

    .big {
        font-size: 20px;
    }
</style>