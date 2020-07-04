<template>
    <div class="card mb-3">
        <div class="card-body bg-light text-soft-dark">
            <div class="row">
                <div class="col-6">
                    <h5>
                        <a href="#" @click.prevent="toggleCards()">
                            <font-awesome-icon v-if="onlyManaged" class="text-success"
                                               :icon="['fas','toggle-on']"></font-awesome-icon>
                            <font-awesome-icon v-if="!onlyManaged" class="text-danger"
                                               :icon="['fas','toggle-off']"></font-awesome-icon>
                        </a>
                        <span v-if="onlyManaged" class="d-none d-sm-inline">Showing Only Managed Bots</span>
                        <span v-if="!onlyManaged" class="d-none d-sm-inline">Showing All Bots</span>
                    </h5>
                </div>
                <div class="col-6 text-right">
                    <h5>
                        <span v-if="latestVersion !== '0.0.0'" class="d-none d-sm-inline">Github latest: {{latestVersion}}</span>
                        <a v-if="!demoServer" href="#" @click.prevent="updateVersion()" class="text-left">
                            <font-awesome-icon class="text-danger" :icon="['fas','level-up-alt']"></font-awesome-icon>
                        </a>
                    </h5>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                onlyManaged: false,
                demoServer: false,
                latestVersion: '0.0.0',
                downloadUrl: ''
            }
        },
        methods: {
            getToggle() {
                axios.get('/api/v1/toggleCards').then((response) => {
                    this.onlyManaged = response.data.onlyManaged
                }).catch((error) => {
                    console.log(error)
                })
            },
            toggleCards() {
                axios.post('/api/v1/toggleCards').then((response) => {
                    this.onlyManaged = response.data.onlyManaged
                }).catch((error) => {
                    console.log(error)
                })
            },
            getToggleData() {
                axios.get('/api/v1/toggleData').then((response) => {
                    this.latestVersion = response.data.latestVersion
                    this.downloadUrl = response.data.downloadUrl
                    this.demoServer = response.data.demoServer
                }).catch((error) => {
                    console.log(error)
                })
            },
            updateVersion() {
                this.$swal.fire({
                    title: 'Do you really want to update your bots?',
                    showCancelButton: true,
                    cancelButtonText: 'Exit',
                    confirmButtonText: 'Update',
                    reverseButtons: true,
                    input: 'text',
                    inputValue: this.downloadUrl
                }).then((result) => {
                    console.log(result.value);
                    if (result.value) {
                        let url = result.value;
                        this.$swal.fire({
                            title: 'Confirm update',
                            text: 'Updating from: ' + url,
                            showCancelButton: true,
                            cancelButtonText: 'Exit',
                            confirmButtonText: 'Update',
                            reverseButtons: true,
                            input: 'checkbox',
                            inputValue: false,
                            inputPlaceholder: "Check to force update"
                        }).then((result) => {
                            console.log(result.value);
                            if (typeof(result.value) !==  'undefined') {
                                axios.post('/api/v1/updateBots?forceUrl=' + url + '&forceUpdate=' + result.value)
                                    .then(() => {
                                        this.$swal.fire('The update procedure has started...');
                                    }).catch((error) => {
                                    this.$swal.fire('You encountered an error: ' + error.response.data.message);
                                })
                            }
                        })
                    }
                })
            },
        },
        mounted() {
            this.getToggle();
            this.getToggleData();
        }
    }
</script>

<style scoped>
</style>