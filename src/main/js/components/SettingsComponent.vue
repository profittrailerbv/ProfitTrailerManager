<template>
    <div class="row">
        <div class="col font-weight-bold">
            <div class="card">
                <div class="card-body bg-light form-group">
                    <div class="row">
                        <div class="col">
                            Global Settings
                        </div>
                    </div>
                    <div class="row mt-3">
                        <div class="col">
                            <label for="tmz">Timezone:</label>
                            <select  v-model="settings.timezone" id="tmz" class="form-control">
                                <option v-for="timezone in timezones" v-bind:value="timezone.id">
                                    {{timezone.value}}
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="row mt-3">
                        <div class="col">
                            <label for="cur">Currency:</label>
                            <input id="cur" class="form-control" name="currency" type="text" v-model="settings.currency"
                                   placeholder="EUR">
                        </div>
                    </div>
                    <div class="row mt-3">
                        <div class="col">
                            <label for="tok">Discord token:</label>
                            <input id="tok" class="form-control" name="token" type="text" v-model="settings.token"
                                   placeholder="">
                        </div>
                    </div>
                    <div class="row mt-4">
                        <div class="col">
                            <a v-if="!demoServer" href="#" @click.prevent="saveSettings()">
                                <button type="button" class="btn btn-success form-control">Save</button>
                            </a>
                        </div>
                    </div>
                    <div class="row mt-2">
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
                demoServer: false,
                timezones: [],
                settings: {
                    timezone: "",
                    currency: "",
                    token: ""
                }
            }
        },
        methods: {
            getSettings() {
                axios.get('/api/v1/globalSettings').then((response) => {
                    this.demoServer = response.data.demoServer;
                    this.timezones = response.data.timezones;
                    this.settings.timezone = response.data.timezone;
                    this.settings.currency = response.data.currency;
                    this.settings.token = response.data.token;
                }).catch((error) => {
                    if (!error.response) {
                        window.location.href = '/';
                    } else {
                        this.error = error.response.data.message;
                    }
                })

            },
            saveSettings() {
                const formData = new FormData();
                formData.append("timezone", this.settings.timezone);
                formData.append("currency", this.settings.currency);
                formData.append("token", this.settings.token);

                axios.post('/api/v1/globalSettings', formData).then(() => {
                }).catch((error) => {
                    this.error = error.response.data.message;
                })
            }
        },
        mounted() {
            this.getSettings()
        },
    }
</script>

<style scoped>
    .form-margin {
        margin-top: 20%;
    }

    .text-soft-danger {
        color: #be584a;
    }
</style>