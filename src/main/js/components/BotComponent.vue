<template>
    <div class="row row-cols-1 row-cols-md-3">
        <div class="col mb-4" v-for="bot in bots">
            <div class="card">
                <div class="card-header">
	                <h5 class="card-title">
	                <i class="fas fa-circle" v-bind:class="getStatusClass(bot.status)"></i> {{ bot.siteName }}
	                </h5>
                  </div>
                <div class="card-body">
                    <p>{{ bot.status }} {{ bot.botProperties.managed }}</p>
                    <p v-if="bot.botProperties.managed && containsKey(bot, 'statsData')"> Today: {{roundNumber(bot.statsData.basic.totalProfitToday, 3)}} ({{bot.statsData.basic.totalProfitPercToday}}%)</p>
                    <p v-if="bot.botProperties.managed && containsKey(bot, 'statsData')"> Yesterday: {{roundNumber(bot.statsData.basic.totalProfitYesterday, 3)}} ({{bot.statsData.basic.totalProfitPercYesterday}}%)</p>
                </div>
                <div class="card-footer text-muted text-right">
                    <a href="#" @click.prevent="restartBot(bot.directory)"><i class="fas fa-redo-alt"></i></a>
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
                })
            },
            stopBot(name) {
                axios.post('/api/v1/stopBot?directoryName=' + name).then((response) => {
                }).catch((error) => {
                    console.log(error)
                })

                this.getBotStatus(name)
            },
            restartBot(name) {
                axios.post('/api/v1/restartBot?directoryName=' + name).then((response) => {
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
            getStatusClass(status) {
                return status === 'ONLINE' ? 'text-success' : 'text-danger';
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