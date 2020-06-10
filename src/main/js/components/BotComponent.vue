<template>
    <div class="bot-component row row-cols-1 row-cols-md-2 row-cols-lg-3 row-cols-xxl-4">
        <div class="col mb-4" v-for="bot in bots">
            <div class="card">
                <div class="card-body bg-light">
                    <div class="row text-muted">
                        <div class="col-10 text-left font-weight-bold pr-0">
                            <font-awesome-icon :class="getStatusClass(bot.status, bot.initialSetup)"
                                               :icon="['fas','circle']"></font-awesome-icon>
                            {{ bot.siteName }}
                            <span class="small"><sup>({{bot.data.exchange}})</sup></span>
                        </div>
                        <div class="col-2 text-right pl-0">
                            <font-awesome-icon :icon="['fas','file-alt']" v-if="bot.data.paper"></font-awesome-icon>
                            <a :href="'api/v1/linkout?directoryName=' + bot.directory" target="_blank"
                               class="text-muted">
                                <font-awesome-icon :icon="['fas','external-link-alt']"></font-awesome-icon>
                            </a>
                        </div>
                    </div>
                    <div class="row text-soft-dark mt-3">
                        <div class="col-6 text-left font-weight-bold">
                            <span class="">Today ({{bot.data.totalSalesToday}})</span><br/>
                        </div>
                        <div class="col-6 text-left font-weight-bold"
                             :class="bot.data.totalProfitToday > 0 ? 'text-soft-success' : bot.data.totalProfitToday < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                            <span v-if="bot.managed && containsKey(bot.data, 'totalProfitToday')"> {{roundNumber(bot.data.totalProfitToday, 3)}}</span>
                            <span v-if="bot.managed && containsKey(bot.data, 'totalProfitPercToday')"
                                  class="small"> ({{bot.data.totalProfitPercToday}}%)</span>
                        </div>
                    </div>
                    <div class="row text-soft-dark">
                        <div class="col-6 text-left font-weight-bold">
                            <span class="">Yesterday ({{bot.data.totalSalesYesterday}})</span><br/>
                        </div>
                        <div class="col-6 text-left font-weight-bold"
                             :class="bot.data.totalProfitYesterday > 0 ? 'text-soft-success' : bot.data.totalProfitYesterday < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                            <span v-if="bot.managed && containsKey(bot.data, 'totalProfitYesterday')"> {{roundNumber(bot.data.totalProfitYesterday, 3)}}</span>
                            <span v-if="bot.managed && containsKey(bot.data, 'totalProfitPercYesterday')"
                                  class="small"> ({{bot.data.totalProfitPercYesterday}}%)</span>
                        </div>
                    </div>
                    <div class="row text-soft-dark">
                        <div class="col-6 text-left font-weight-bold">
                            <span class="">Current Diff</span><br/>
                        </div>
                        <div class="col-6 text-left font-weight-bold pl-6"
                             :class="bot.data.diff > 0 ? 'text-soft-success' : bot.data.diff < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                            <span v-if="bot.managed && containsKey(bot.data, 'pairsTotal')"> {{roundNumber(bot.data.diff, 4)}}</span>
                            <span>{{bot.data.market}}</span>
                        </div>
                    </div>
                    <div class="row text-soft-dark mt-3">
                        <div class="col-12 text-left">
                            <span v-if="bot.managed && containsKey(bot.data, 'lastSaleMinutes')"> Last sale {{bot.data.lastSaleMinutes}} minutes ago ({{bot.data.lastSaleProfit}}%) </span>
                            <span v-if="!bot.managed || !containsKey(bot.data, 'lastSaleMinutes')">&nbsp;</span>
                        </div>
                    </div>
                </div>
                <div style="border-width:5px" class="card-footer border-dark"
                     :class="bot.data.totalProfitAllTime > 0 ? 'bg-soft-green' : bot.data.totalProfitAllTime < 0 ? 'bg-soft-red' : ''">
                    <div class="row" :class="bot.data.totalProfitAllTime == 0 ? 'text-muted' : 'text-soft-dark'">
                        <div class="col-6 text-left font-weight-bold font-italic">
                            <span class="">Profit All Time</span><br/>
                        </div>
                        <div class="col-6 text-right font-weight-bold pl-6"
                             :class="bot.data.totalProfitAllTime > 0 ? 'text-soft-success' : bot.data.totalProfitAllTime < 0 ? 'text-soft-danger' : 'text-secondary'">
                            <span v-if="bot.managed && containsKey(bot.data, 'totalProfitAllTime')"> {{roundNumber(bot.data.totalProfitAllTime, 3)}}</span>
                            <span v-if="bot.managed && containsKey(bot.data, 'totalProfitPercAllTime')"
                                  class="small"> ({{bot.data.totalProfitPercAllTime}}%)</span>
                        </div>
                    </div>
                    <div class="row mt-3 small"
                         :class="bot.data.totalProfitAllTime == 0 ? 'text-muted' : 'text-soft-dark'">
                        <div class="col-4 text-left">
                            <span v-if="bot.managed && containsKey(bot.data, 'pairsTotal')"> PAIRS: {{bot.data.pairsTotal}}</span>
                        </div>
                        <div class="col-8 text-right">
                            <span v-if="bot.managed && containsKey(bot.data, 'balance')"> BAL: {{roundNumber(bot.data.balance, 6)}}</span>
                        </div>
                    </div>
                    <div class="row small" :class="bot.data.totalProfitAllTime == 0 ? 'text-muted' : 'text-soft-dark'">
                        <div class="col-4 text-left">
                            <span v-if="bot.managed && containsKey(bot.data, 'dcaTotal')"> DCA: {{bot.data.dcaTotal}}</span>
                        </div>
                        <div class="col-8 text-right">
                            <span v-if="bot.managed && containsKey(bot.data, 'tcv')"> TCV: {{roundNumber(bot.data.tcv, 6)}}</span>
                        </div>
                    </div>

                    <div class="row text-right small mt-2"
                         :class="bot.data.totalProfitAllTime == 0 ? 'text-muted' : 'text-soft-dark'">
                        <div class="col-12">
                            <span v-if="bot.managed && containsKey(bot.data, 'version')" class="smaller"> V{{bot.data.version}}</span>
                            <a v-if="!demoServer && bot.managed" href="#"
                               @click.prevent="restartBot(bot.directory, bot.siteName)">
                                <font-awesome-icon :icon="['fas','redo-alt']" class="text-dark"></font-awesome-icon>
                            </a>
                            <a v-if="!demoServer && !bot.managed" href="#"
                               @click.prevent="manageBot(bot.directory, bot.siteName)">
                                <button type="button" class="btn btn-primary small">Manage</button>
                            </a>
                            <a v-if="!demoServer && bot.managed" href="#"
                               @click.prevent="updateVersion(bot.directory, bot.siteName)"
                               class="text-left ml-1">
                                <font-awesome-icon class="text-danger"
                                                   :icon="['fas','level-up-alt']"></font-awesome-icon>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row text-info">
            <div class="col text-left font-weight-bold pr-0 d-flex justify-content-center align-items-center mb-5">
                <a v-if="!demoServer" href="#" @click.prevent="createNewBot()">
                    <font-awesome-icon class="display-1"
                                       :icon="['fas','plus-square']"></font-awesome-icon>
                </a>
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
                timer: '',
                demoServer: false,
                downloadUrl: ''
            }
        },
        created() {
            this.timer = setInterval(this.getAllBots, 5000)
        },
        methods: {
            getAllBots() {
                axios.get('/api/v1/data').then((response) => {
                    this.bots = response.data.bots
                    this.demoServer = response.data.demoServer
                    this.downloadUrl = response.data.downloadUrl
                }).catch((error) => {
                    if (!error.response) {
                        window.location.href = '/';
                    }
                })
            },
            restartBot(name, siteName) {
                this.$swal.fire({
                    title: 'Confirm restart',
                    text: 'Do you really want to restart ' + siteName + '?',
                    showCancelButton: true,
                    cancelButtonText: 'Exit',
                    confirmButtonText: 'Restart',
                    reverseButtons: true
                }).then((result) => {
                    if (typeof (result.value) !== 'undefined') {
                        axios.post('/api/v1/restartBot?directoryName=' + name).then(() => {
                            this.$swal.fire('The restart command has been sent...');
                            this.getBotStatus(name);
                        }).catch((error) => {
                            this.$swal.fire(error.response.data.message);
                        })
                    }
                })
            },
            manageBot(name, siteName) {
                this.$swal.fire({
                    title: 'Confirm manage',
                    text: 'Manage this bot ' + siteName + '? It will be restarted.',
                    showCancelButton: true,
                    cancelButtonText: 'Exit',
                    confirmButtonText: 'Manage',
                    reverseButtons: true
                }).then((result) => {
                    if (typeof (result.value) !== 'undefined') {
                        axios.post('/api/v1/restartBot?directoryName=' + name).then(() => {
                            this.$swal.fire('The manage command has been sent...');
                            this.getBotStatus(name);
                        }).catch((error) => {
                            this.$swal.fire(error.response.data.message);
                        })
                    }
                })
                this.getBotStatus(name)
            },
            getBotStatus(name) {
                axios.get('/api/v1/status?directoryName=' + name).then(() => {
                }).catch((error) => {
                    console.log(error)
                })
            },
            getStatusClass(status, initialSetup) {
                if (initialSetup) {
                    return 'text-info';
                }
                return status === 'ONLINE'
                    ? 'text-soft-success'
                    : status === 'INITIALIZING' || status === 'RESTARTING' || status === 'UPDATING'
                        ? 'text-warning'
                        : 'text-soft-danger';
            },
            containsKey(obj, key) {
                return Object.keys(obj).includes(key);
            },
            roundNumber(number, decimals) {
                return number.toFixed(decimals);
            },
            createNewBot() {
                this.$swal.fire({
                    title: 'Create new bot',
                    text: 'Enter the directory name for your new bot (No spaces)',
                    showCancelButton: true,
                    cancelButtonText: 'Exit',
                    confirmButtonText: 'Create',
                    reverseButtons: true,
                    input: 'text',
                    inputValue: false,
                    inputPlaceholder: "binancebot"
                }).then((result) => {
                    if (result.value) {
                        axios.post('/api/v1/createNewBot?directoryName=' + result.value)
                            .then(() => {
                                this.$swal.fire('Your bot will be created and started...');
                            }).catch((error) => {
                            this.$swal.fire(error.response.data.message);
                        })
                    }
                })
            },
            updateVersion(name, siteName) {
                this.$swal.fire({
                    title: 'Update ' + siteName + '?',
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
                        axios.post('/api/v1/updateBots?forceUrl=' + url + '&directoryName=' + name)
                            .then(() => {
                                this.$swal.fire('The update procedure has started...');
                            }).catch((error) => {
                            this.$swal.fire('You encountered an error: ' + error.response.data.message);
                        })
                    }
                })
            }
        },
        mounted() {
            this.getAllBots()
        },
        beforeDestroy() {
            clearInterval(this.timer)
        }
    }
</script>

<style scoped>
    .bg-soft-green {
        background-color: #c2efa2;
    }

    .bg-soft-red {
        background-color: #e3a09c;
    }

    .text-soft-danger {
        color: #be584a;
    }

    .text-soft-success {
        color: #61a242;
    }

    .smaller {
        font-size: 11px;
    }
</style>