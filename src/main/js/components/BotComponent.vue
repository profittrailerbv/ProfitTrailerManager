<template>
    <div class="row row-cols-1 row-cols-md-3">
        <div class="col mb-4" v-for="bot in bots">
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">{{ bot.siteName }} <i class="fas fa-check-square"></i></h5>
                    <p>{{ bot.status }} {{ bot.botProperties.managed }}</p>
                    <p><a href="#" @click.prevent="stopBot(bot.directory)">Stop Bot</a></p>
                    <p><a href="#" @click.prevent="restartBot(bot.directory)">Restart Bot</a></p>
                    <p v-if="bot.botProperties.managed && containsKey(bot, 'statsData')"> Today: {{roundNumber(bot.statsData.basic.totalProfitToday, 3)}} ({{bot.statsData.basic.totalProfitPercToday}}%)</p>
                    <p v-if="bot.botProperties.managed && containsKey(bot, 'statsData')"> Yesterday: {{roundNumber(bot.statsData.basic.totalProfitYesterday, 3)}} ({{bot.statsData.basic.totalProfitPercYesterday}}%)</p>
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
                timer: ''
            }
        },
        created() {
            this.timer = setInterval(this.getAllBots, 5000)
        },
        methods: {
            getAllBots() {
                axios.get('/api/v1/data').then((response) => {
                    this.bots = response.data.bots
                    console.log(response)
                })
            },
            stopBot(name) {
                axios.post('/api/v1/stopBot?directoryName=' + name).then((response) => {
                    console.log(response)
                }).catch((error) => {
                    console.log(error)
                })

                this.getBotStatus(name)
            },
            restartBot(name) {
                axios.post('/api/v1/restartBot?directoryName=' + name).then((response) => {
                    console.log(response)
                }).catch((error) => {
                    console.log(error)
                })

                this.getBotStatus(name)
            },
            getBotStatus(name) {
                axios.get('/api/v1/status?directoryName=' + name).then((response) => {
                    console.log(response)
                }).catch((error) => {
                    console.log(error)
                })
            },
            containsKey(obj, key ) {
                return Object.keys(obj).includes(key);
            },
            roundNumber(number, decimals) {
                return number.toFixed(decimals);
            }
        },
        mounted() {
            this.getAllBots()
        },
        beforeDestroy () {
            clearInterval(this.timer)
        }
    }
</script>

<style scoped>

</style>