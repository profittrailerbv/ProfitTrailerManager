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
              <span v-if="bot.managed && containsKey(bot.data, 'version')"
                    class="smaller"><sup>V{{ bot.data.version }}</sup></span>
            </div>
            <div class="col-2 text-right pl-0">
              <font-awesome-icon :icon="['fas','file-alt']" v-if="bot.data.paper"></font-awesome-icon>
              <a :href="'api/v1/linkout?directoryName=' + bot.directory" target="_blank"
                 class="text-muted">
                <font-awesome-icon :icon="['fas','external-link-alt']"></font-awesome-icon>
              </a>
            </div>
          </div>
          <div class="row small" :class="bot.data.totalCombinedAllTime === 0 ? 'text-muted' : 'text-soft-dark'">
            <div class="col-12 text-left">
              {{ bot.data.config }} <span class="smallest">({{ bot.data.exchange }})</span>
            </div>
          </div>
          <div class="row text-soft-dark mt-3">
            <div class="col-6 text-left font-weight-bold">
              <span class="">Today ({{ bot.data.totalSalesToday }})</span><br/>
            </div>
            <div class="col-6 text-right font-weight-bold"
                 :class="bot.data.totalCombinedToday > 0 ? 'text-soft-success' : bot.data.totalCombinedToday < 0 ? 'text-soft-danger' : 'text-soft-dark'">
              <span
                  v-if="bot.managed && containsKey(bot.data, 'totalCombinedToday')"> {{
                  roundNumber(bot.data.totalCombinedToday, bot.data.guiPrecision)
                }}</span>
              <span v-if="bot.managed && containsKey(bot.data, 'totalCombinedPercToday')"
                    class="small"> ({{ roundNumber(bot.data.totalCombinedPercToday , 2) }}%)</span>
            </div>
          </div>
          <div class="row text-soft-dark">
            <div class="col-6 text-left font-weight-bold">
              <span class="">Yesterday ({{ bot.data.totalSalesYesterday }})</span><br/>
            </div>
            <div class="col-6 text-right font-weight-bold"
                 :class="bot.data.totalCombinedYesterday > 0 ? 'text-soft-success' : bot.data.totalCombinedYesterday < 0 ? 'text-soft-danger' : 'text-soft-dark'">
              <span
                  v-if="bot.managed && containsKey(bot.data, 'totalCombinedYesterday')"> {{
                  roundNumber(bot.data.totalCombinedYesterday, bot.data.guiPrecision)
                }}</span>
              <span v-if="bot.managed && containsKey(bot.data, 'totalCombinedPercYesterday')"
                    class="small"> ({{ roundNumber(bot.data.totalCombinedPercYesterday , 2) }}%)</span>
            </div>
          </div>
          <div class="row text-soft-dark">
            <div class="col-6 text-left font-weight-bold">
              <span class="">Current Diff</span><br/>
            </div>
            <div class="col-6 text-right font-weight-bold pl-6"
                 :class="bot.data.diff > 0 ? 'text-soft-success' : bot.data.diff < 0 ? 'text-soft-danger' : 'text-soft-dark'">
              <span
                  v-if="bot.managed && containsKey(bot.data, 'pairsTotal')"> {{ roundNumber(bot.data.diff, bot.data.guiPrecision) }}</span>
              <span>{{ bot.data.market }}</span>
            </div>
          </div>
          <div class="row text-soft-dark mt-3">
            <div class="col-12 text-left">
              <span
                  v-if="bot.managed && containsKey(bot.data, 'lastSaleMinutesString')"> Last sale {{
                  bot.data.lastSaleMinutesString}} ({{ roundNumber(bot.data.lastSaleProfit, 2) }}%) </span>
              <span v-if="!bot.managed || !containsKey(bot.data, 'lastSaleMinutesString')">&nbsp;</span>
            </div>
          </div>
        </div>
        <div style="border-width:5px" class="card-footer border-dark"
             :class="bot.data.totalCombinedAllTime > 0 ? 'bg-soft-green' : bot.data.totalCombinedAllTime < 0 ? 'bg-soft-red' : ''">
          <div class="row" :class="bot.data.totalCombinedAllTime === 0 ? 'text-muted' : 'text-soft-dark'">
            <div class="col-5 text-left font-weight-bold font-italic pr-0">
              <span class="">Profit All Time</span><br/>
            </div>
            <div class="col-7 text-right font-weight-bold pl-3"
                 :class="bot.data.totalCombinedAllTime > 0 ? 'text-soft-success' : bot.data.totalCombinedAllTime < 0 ? 'text-soft-danger' : 'text-secondary'">
              <span
                  v-if="bot.managed && containsKey(bot.data, 'totalCombinedAllTime')"> {{
                  roundNumber(bot.data.totalCombinedAllTime, bot.data.guiPrecision)
                }}</span>
              <span v-if="bot.managed && containsKey(bot.data, 'totalCombinedPercAllTime')"
                    class="small"> ({{ roundNumber(bot.data.totalCombinedPercAllTime, 2) }}%)</span>
            </div>
          </div>
          <div class="row mt-3 small"
               :class="bot.data.totalCombinedAllTime === 0 ? 'text-muted' : 'text-soft-dark'">
            <div class="col-4 text-left">
              <span v-if="bot.managed && containsKey(bot.data, 'pairsTotal')"> PAIRS: {{ bot.data.pairsTotal }}</span>
            </div>
            <div class="col-8 text-right">
              <span
                  v-if="bot.managed && containsKey(bot.data, 'balance')"> BAL: {{
                  roundNumber(bot.data.balance, bot.data.guiPrecision)
                }}</span>
            </div>
          </div>
          <div class="row small" :class="bot.data.totalCombinedAllTime === 0 ? 'text-muted' : 'text-soft-dark'">
            <div class="col-4 text-left">
              <span v-if="bot.managed && containsKey(bot.data, 'dcaTotal')"> DCA: {{ bot.data.dcaTotal }}</span>
            </div>
            <div class="col-8 text-right">
              <span v-if="bot.managed && containsKey(bot.data, 'tcv')"> TCV: {{ roundNumber(bot.data.tcv, bot.data.guiPrecision) }}</span>
            </div>
          </div>
          <div class="row small" :class="bot.data.totalCombinedAllTime === 0 ? 'text-muted' : 'text-soft-dark'">
            <div class="col-4 text-left">
              <span v-if="bot.managed && containsKey(bot.data, 'pendingTotal')"> PENDING: {{ bot.data.pendingTotal }}</span>
            </div>
            <div class="col-8 text-right">
              <span v-if="bot.managed && containsKey(bot.data, 'score')"> SCORE: {{ roundNumber(bot.data.score, bot.data.guiPrecision) }}</span>
            </div>
          </div>
        </div>
        <div style="border-width:1px" class="card-footer border-dark"
             :class="bot.data.totalCombinedAllTime > 0 ? 'bg-soft-green' : bot.data.totalCombinedAllTime < 0 ? 'bg-soft-red' : ''">
          <div class="row">
            <div class="col-6 text-left">
              <a v-if="!demoServer && bot.managed" href="#"
                 @click.prevent="toggleSOM(bot.directory, bot.siteName, !bot.data.sellOnlyMode)">
                <font-awesome-icon :icon="['fas','money-check-alt']"
                                   :class="getSOMClass(bot.data.sellOnlyMode)"></font-awesome-icon>
              </a>
<!--              C: {{bot.cpu}}-->
<!--              R: {{roundNumber(bot.ram/1000,0)}}-->
            </div>
            <div class="col-6 text-right">
              <a v-if="!demoServer && bot.managed" href="#"
                 @click.prevent="restartBot(bot.directory, bot.siteName)">
                <font-awesome-icon :icon="['fas','redo-alt']" class="text-dark"></font-awesome-icon>
              </a>
              <a v-if="!demoServer && bot.managed" href="#"
                 @click.prevent="stopBot(bot.directory, bot.siteName)">
                <font-awesome-icon :icon="['fas','stop-circle']" class="text-dark"></font-awesome-icon>
              </a>
              <span class="border border-dark border-right mx-1"></span>
              <a v-if="!demoServer && !bot.managed" href="#"
                 @click.prevent="manageBot(bot.directory, bot.siteName)">
                <button type="button" class="btn btn-primary small">Manage</button>
              </a>
              <span v-if="!demoServer && !bot.managed" class="border border-dark border-right mx-1"></span>
              <a v-if="!demoServer && !bot.managed" href="#"
                 @click.prevent="deleteBot(bot.directory, bot.siteName)"
                 class="text-left ml-1">
                <font-awesome-icon class="text-danger"
                                   :icon="['fas','trash']"></font-awesome-icon>
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
        <a v-if="!demoServer && !maxBotsReached" href="#" @click.prevent="createNewBot()">
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
      downloadUrl: '',
      maxBotsReached: false
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
        this.maxBotsReached = response.data.maxBotsReached
      }).catch((error) => {
        if (!error.response || error.response.status === 502) {
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
    stopBot(name, siteName) {
      this.$swal.fire({
        title: 'Confirm stop',
        text: 'Do you really want to stop ' + siteName + '?',
        showCancelButton: true,
        cancelButtonText: 'Exit',
        confirmButtonText: 'Stop',
        reverseButtons: true
      }).then((result) => {
        if (typeof (result.value) !== 'undefined') {
          axios.post('/api/v1/stopBotAndUnlink?directoryName=' + name).then(() => {
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
    deleteBot(name, siteName) {
      this.$swal.fire({
        title: 'Confirm delete',
        text: 'Delete this bot ' + siteName + '? The folder will deleted. This action cannot be reverted',
        showCancelButton: true,
        cancelButtonText: 'Exit',
        confirmButtonText: 'Delete',
        reverseButtons: true
      }).then((result) => {
        if (typeof (result.value) !== 'undefined') {
          axios.post('/api/v1/deleteBot?directoryName=' + name).then(() => {
            this.$swal.fire('The delete command has been sent...');
            this.getAllBots();
          }).catch((error) => {
            this.$swal.fire(error.response.data.message);
          })
        }
      })
      this.getAllBots();
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
    getSOMClass(sellOnlyMode) {
      return sellOnlyMode ? 'text-danger' : 'text-dark';
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
    },
    toggleSOM(name, siteName, newStatus) {
      let status = newStatus ? 'enable' : 'disable';
      this.$swal.fire({
        title: 'Confirm SOM',
        text: 'Do you really want to ' + status + ' Global SOM for ' + siteName + '?',
        showCancelButton: true,
        cancelButtonText: 'Exit',
        confirmButtonText: 'Confirm',
        reverseButtons: true
      }).then((result) => {
        if (typeof (result.value) !== 'undefined') {
          axios.post('/api/v1/toggleSOM?directoryName=' + name + "&enabled=" + newStatus).then(() => {
            this.getBotStatus(name);
          }).catch((error) => {
            this.$swal.fire(error.response.data.message);
          })
        }
      })
    },
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

.smaller {
  font-size: 11px;
}
.smallest {
  font-size: 9px;
}
</style>