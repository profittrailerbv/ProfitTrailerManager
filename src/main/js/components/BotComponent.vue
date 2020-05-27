<template>
    <div class="row row-cols-1 row-cols-md-3">
        <div class="col mb-4" v-for="bot in bots">
            <div class="card">
                <div class="card-header">
	                <h5 class="card-title">
	                    <div class="row">
		                    <div class="col-10 text-left">
	                            <font-awesome-icon :class="getStatusClass(bot.status)" :icon="['fas','circle']"></font-awesome-icon> {{ bot.siteName }}
	                        </div>
	                        <div class="col-2 text-right">
	                            <a :href="bot.url" target="_blank">
	                                <font-awesome-icon :icon="['fas','external-link-alt']"></font-awesome-icon>
	                            </a>
	                        </div>
                        </div>
	                </h5>
                  </div>
                <div class="card-body">
                    <p v-if="bot.botProperties.managed && containsKey(bot.data, 'totalProfitToday')"> Today: {{roundNumber(bot.data.totalProfitToday, 3)}} ({{bot.data.totalProfitPercToday}}%)</p>
                    <p v-if="bot.botProperties.managed && containsKey(bot.data, 'totalProfitYesterday')"> Yesterday: {{roundNumber(bot.data.totalProfitYesterday, 3)}} ({{bot.data.totalProfitPercYesterday}}%)</p>
                </div>
                <div class="card-footer text-muted">
                    <div class="row">
	                    <div class="col-10" style="font-size:12px">
	                        <p v-if="bot.botProperties.managed && containsKey(bot.data, 'version')"> Version: {{bot.data.version}}</p>
	                    </div>
	                    <div class="col-2 text-right">
		                     <a href="#" @click.prevent="restartBot(bot.directory)">
		                         <font-awesome-icon :icon="['fas','redo-alt']"></font-awesome-icon>
		                     </a>
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
                return status === 'ONLINE' ? 'text-success' : status === 'INITIALIZING' ? 'text-warning' : 'text-danger';
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