<template>

    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" v-if="title" href="/">ProfitTrailer Manager<sup> v{{version}}</sup> -- {{title}}</a>
            <a class="navbar-brand" v-else href="/">ProfitTrailer Manager<sup> v{{version}}</sup></a>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent"
                    aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                <ul class="navbar-nav ml-auto">
                    <li class="nav-item active">
                        <a class="nav-link" href="/">Overview</a>
                    </li>
                    <li class="nav-item active">
                        <a class="nav-link" href="settings">Settings</a>
                    </li>
                    <li class="nav-item active">
                        <a class="nav-link" href="#" @click.prevent="shutdownApp()">
                            <font-awesome-icon :icon="['fas','power-off']"></font-awesome-icon>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

</template>

<script>
    export default {
        data() {
            return {
                version: "n/a",
                bots: [],
                loading: true,
            }
        },
        props: ['title'],
        methods: {
            getAllBots() {
                axios.get('/api/v1/data').then((response) => {
                    this.bots = response.data.bots;
                    this.version = response.data.version;
                })
            },
            startBot(name) {
                axios.post('/api/v1/startBot?directoryName=' + name).then((response) => {
                }).catch((error) => {
                    console.log(error)
                })

                this.getBotStatus(name)
            },
            getBotStatus(name) {
                axios.get('/api/v1/status?directoryName=' + name).then((response) => {
                }).catch((error) => {
                    console.log(error)
                })
            },
            shutdownApp() {
                axios.post('/api/v1/shutdown').then((response) => {
                }).catch((error) => {
                    console.log(error)
                })
            }
        },
        mounted() {
            this.getAllBots()
        }
    }
</script>

<style scoped>

</style>