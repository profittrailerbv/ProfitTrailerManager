<template>
    <div class="row row-cols-1 row-cols-md-3">
        <div class="col mb-4" v-for="bot in bots">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">{{ bot.siteName }}</h5>
                    <p>{{ bot.status }} {{ bot.botProperties.managed }}</p>
                    <p><a href="#" @click.prevent="getBotStatus(bot.directory)">Refresh Status</a></p>
                    <p><a href="#" @click.prevent="stopBot(bot.directory)">Stop Bot</a></p>
                    <p><a href="#" @click.prevent="startBot(bot.directory)">Start Bot</a></p>
                    <p v-if="containsKey(bot, 'statsData')"> Today: {{bot.statsData.basic.totalProfitToday}} ({{bot.statsData.basic.totalProfitPercToday}}%)</p>
                    <p v-if="containsKey(bot, 'statsData')"> Yesterday: {{bot.statsData.basic.totalProfitYesterday}} ({{bot.statsData.basic.totalProfitPercYesterday}}%)</p>
                    <p class="card-text">This is a longer card with supporting text below as a natural lead-in to
                        additional content. This content is a little bit longer.</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                bots: [],
                loading: true,
            }
        },
        methods: {
            getAllBots() {
                axios.get('/api/v1/data').then((response) => {
                    this.bots = response.data.bots
                    console.log(response)
                })
            },
            stopBot(name) {
                axios.post('/api/v1/stopBot?botName=' + name).then((response) => {
                    console.log(response)
                }).catch((error) => {
                    console.log(error)
                })

                this.getBotStatus(name)
            },
            startBot(name) {
                axios.post('/api/v1/startBot?botName=' + name).then((response) => {
                    console.log(response)
                }).catch((error) => {
                    console.log(error)
                })

                this.getBotStatus(name)
            },
            getBotStatus(name) {
                axios.get('/api/v1/status?botName=' + name).then((response) => {
                    console.log(response)
                }).catch((error) => {
                    console.log(error)
                })
            },
            containsKey(obj, key ) {
                return Object.keys(obj).includes(key);
            }
        },
        mounted() {
            this.getAllBots()
        }
    }
</script>

<style scoped>

</style>