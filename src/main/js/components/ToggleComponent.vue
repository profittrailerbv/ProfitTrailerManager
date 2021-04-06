<template>
  <div class="card mb-3">
    <div class="card-body bg-light text-soft-dark">
      <div class="row">
        <div class="col-6">
          <div class="row">
            <div class="col-1">
              <a href="#" @click.prevent="toggleCards()">
                <font-awesome-icon v-if="onlyManaged" class="text-success"
                                   :icon="['fa','robot']"></font-awesome-icon>
                <font-awesome-icon v-if="!onlyManaged" class="text-danger"
                                   :icon="['fa','robot']"></font-awesome-icon>
              </a>
            </div>
            <div class="col-1">
              <a href="#" @click.prevent="toggleAmount()">
                <font-awesome-icon v-if="showAmount" class="text-success"
                                   :icon="['fa','coins']"></font-awesome-icon>
                <font-awesome-icon v-if="!showAmount" class="text-danger"
                                   :icon="['fa','coins']"></font-awesome-icon>
              </a>
            </div>
            <div class="col-1">
              <a href="#" @click.prevent="togglePercentage()">
                <font-awesome-icon v-if="showPercentage" class="text-success"
                                   :icon="['fa','percentage']"></font-awesome-icon>
                <font-awesome-icon v-if="!showPercentage" class="text-danger"
                                   :icon="['fa','percentage']"></font-awesome-icon>
              </a>
            </div>
          </div>
        </div>
        <div class="col-6 text-right">
          <h5>
            <span v-if="latestVersion !== '0.0.0'" class="d-none d-sm-inline ">Github latest: {{ latestVersion }}</span>
            <a v-if="!demoServer" href="#" @click.prevent="updateVersion()" class="text-left">
              <font-awesome-icon class="text-danger" :icon="['fas','level-up-alt']"></font-awesome-icon>
            </a>
            <span class="border border-dark border-right mx-1"></span>
            <a v-if="!demoServer" href="#"
               @click.prevent="restartBots()">
              <font-awesome-icon :icon="['fas','redo-alt']" class="text-dark"></font-awesome-icon>
            </a>
          </h5>
        </div>
      </div>
      <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 row-cols-xxl-4 pl-3 pr-3">
        <div class="card pl-1 mt-1">
          <div class="col-12 text-left font-weight-bold">
            <div class="row text-soft-dark mb-xxs-2 mb-sm-0">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">Today (L)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalCombinedTodayLive')"
                    :class="globalStats.totalCombinedTodayLive > 0 ? 'text-soft-success' : globalStats.totalCombinedTodayLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundSensitiveNumber(globalStats.totalCombinedTodayLive, 2) }} {{ globalStats.currency }}
              </span>
                <span
                    v-if="containsKey(globalStats, 'totalCombinedTodayLive') && globalStats.totalTCVLive > 0 && showPercentage"
                    :class="globalStats.totalCombinedTodayLive > 0 ? 'text-soft-success' : globalStats.totalCombinedTodayLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalCombinedTodayLive / globalStats.totalTCVLive * 100, 2) }}%)
              </span>
              </div>
            </div>
            <div class="row text-soft-dark">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">Today (T)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalCombinedTodayTest')"
                    :class="globalStats.totalCombinedTodayTest > 0 ? 'text-soft-success' : globalStats.totalCombinedTodayTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundSensitiveNumber(globalStats.totalCombinedTodayTest, 2) }} {{ globalStats.currency }}
              </span>
                <span
                    v-if="containsKey(globalStats, 'totalCombinedTodayTest') && globalStats.totalTCVTest > 0 && showPercentage"
                    :class="globalStats.totalCombinedTodayTest > 0 ? 'text-soft-success' : globalStats.totalCombinedTodayTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalCombinedTodayTest / globalStats.totalTCVTest * 100, 2) }}%)
              </span>
              </div>
            </div>
          </div>
        </div>
        <div class="card pl-1 mt-1">
          <div class="col-12 text-left font-weight-bold">
            <div class="row text-soft-dark mb-xxs-2 mb-sm-0">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">Month (L)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalCombinedThisMonthLive')"
                    :class="globalStats.totalCombinedThisMonthLive > 0 ? 'text-soft-success' : globalStats.totalCombinedThisMonthLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundSensitiveNumber(globalStats.totalCombinedThisMonthLive, 2) }} {{ globalStats.currency }}
              </span>
                <span
                    v-if="containsKey(globalStats, 'totalCombinedThisMonthLive') && globalStats.totalTCVLive > 0 && showPercentage"
                    :class="globalStats.totalCombinedThisMonthLive > 0 ? 'text-soft-success' : globalStats.totalCombinedThisMonthLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalCombinedThisMonthLive / globalStats.totalTCVLive * 100, 2) }}%)
              </span>
              </div>
            </div>
            <div class="row text-soft-dark">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">Month (T)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalCombinedThisMonthTest')"
                    :class="globalStats.totalCombinedThisMonthTest > 0 ? 'text-soft-success' : globalStats.totalCombinedThisMonthTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundSensitiveNumber(globalStats.totalCombinedThisMonthTest, 2) }} {{ globalStats.currency }}
              </span>
                <span
                    v-if="containsKey(globalStats, 'totalCombinedThisMonthTest') && globalStats.totalTCVTest > 0 && showPercentage"
                    :class="globalStats.totalCombinedThisMonthTest > 0 ? 'text-soft-success' : globalStats.totalCombinedThisMonthTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalCombinedThisMonthTest / globalStats.totalTCVTest * 100, 2) }}%)
              </span>
              </div>
            </div>
          </div>
        </div>
        <div class="card pl-1 mt-1">
          <div class="col-12 text-left font-weight-bold">
            <div class="row text-soft-dark mb-xxs-2 mb-sm-0">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">P-Month (L)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalCombinedLastMonthLive')"
                    :class="globalStats.totalCombinedLastMonthLive > 0 ? 'text-soft-success' : globalStats.totalCombinedLastMonthLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundSensitiveNumber(globalStats.totalCombinedLastMonthLive, 2) }} {{ globalStats.currency }}
              </span>
                <span
                    v-if="containsKey(globalStats, 'totalCombinedLastMonthLive') && globalStats.totalTCVLive > 0 && showPercentage"
                    :class="globalStats.totalCombinedLastMonthLive > 0 ? 'text-soft-success' : globalStats.totalCombinedLastMonthLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalCombinedLastMonthLive / globalStats.totalTCVLive * 100, 2) }}%)
              </span>
              </div>
            </div>
            <div class="row text-soft-dark">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">P-Month (T)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalCombinedLastMonthTest')"
                    :class="globalStats.totalCombinedLastMonthTest > 0 ? 'text-soft-success' : globalStats.totalCombinedLastMonthTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundSensitiveNumber(globalStats.totalCombinedLastMonthTest, 2) }} {{ globalStats.currency }}
              </span>
                <span
                    v-if="containsKey(globalStats, 'totalCombinedLastMonthTest') && globalStats.totalTCVTest > 0 && showPercentage"
                    :class="globalStats.totalCombinedLastMonthTest > 0 ? 'text-soft-success' : globalStats.totalCombinedLastMonthTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalCombinedLastMonthTest / globalStats.totalTCVTest * 100, 2) }}%)
              </span>
              </div>
            </div>
          </div>
        </div>
        <div class="card pl-1 mt-1">
          <div class="col-12 text-left font-weight-bold">
            <div class="row text-soft-dark mb-xxs-2 mb-sm-0">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">All (L)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalCombinedAllTimeLive')"
                    :class="globalStats.totalCombinedAllTimeLive > 0 ? 'text-soft-success' : globalStats.totalCombinedAllTimeLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundSensitiveNumber(globalStats.totalCombinedAllTimeLive, 2) }} {{ globalStats.currency }}
              </span>
                <span
                    v-if="containsKey(globalStats, 'totalCombinedAllTimeLive') && globalStats.totalTCVLive > 0 && showPercentage"
                    :class="globalStats.totalCombinedAllTimeLive > 0 ? 'text-soft-success' : globalStats.totalCombinedAllTimeLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalCombinedAllTimeLive / globalStats.totalTCVLive * 100, 2) }}%)
              </span>
              </div>
            </div>
            <div class="row text-soft-dark">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">All (T)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalCombinedAllTimeTest')"
                    :class="globalStats.totalCombinedAllTimeTest > 0 ? 'text-soft-success' : globalStats.totalCombinedAllTimeTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundSensitiveNumber(globalStats.totalCombinedAllTimeTest, 2) }} {{ globalStats.currency }}
              </span>
                <span
                    v-if="containsKey(globalStats, 'totalCombinedAllTimeTest') && globalStats.totalTCVTest > 0 && showPercentage"
                    :class="globalStats.totalCombinedAllTimeTest > 0 ? 'text-soft-success' : globalStats.totalCombinedAllTimeTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalCombinedAllTimeTest / globalStats.totalTCVTest * 100, 2) }}%)
              </span>
              </div>
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
      timer: '',
      onlyManaged: false,
      showAmount: true,
      showPercentage: true,
      demoServer: false,
      latestVersion: '0.0.0',
      downloadUrl: '',
      globalStats: {}
    }
  },
  created() {
    this.timer = setInterval(this.getGlobalStats, 5000)
  },
  methods: {
    getGlobalStats() {
      axios.get('/api/v1/globalStats').then((response) => {
        this.globalStats = response.data
      }).catch((error) => {
        console.log(error)
      })
    },
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
    getShowAmount() {
      axios.get('/api/v1/toggleAmount').then((response) => {
        this.showAmount = response.data.showAmount
      }).catch((error) => {
        console.log(error)
      })
    },
    toggleAmount() {
      axios.post('/api/v1/toggleAmount').then((response) => {
        this.showAmount = response.data.showAmount
      }).catch((error) => {
        console.log(error)
      })
    },
    getShowPercentage() {
      axios.get('/api/v1/togglePercentage').then((response) => {
        this.showPercentage = response.data.showPercentage
      }).catch((error) => {
        console.log(error)
      })
    },
    togglePercentage() {
      axios.post('/api/v1/togglePercentage').then((response) => {
        this.showPercentage = response.data.showPercentage
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
            reverseButtons: true
          }).then((result) => {
            axios.post('/api/v1/updateBots?forceUrl=' + url)
                .then(() => {
                  this.$swal.fire('The update procedure has started...');
                }).catch((error) => {
              this.$swal.fire('You encountered an error: ' + error.response.data.message);
            })
          })
        }
      })
    },
    restartBots() {
      this.$swal.fire({
        title: 'Do you really want to restart your bots?',
        showCancelButton: true,
        cancelButtonText: 'Exit',
        confirmButtonText: 'Restart All',
        reverseButtons: true
      }).then((result) => {
        if (result.value) {
          axios.post('/api/v1/restartBots')
              .then(() => {
                this.$swal.fire('We are currently restarting all bots...');
              }).catch((error) => {
            this.$swal.fire('You encountered an error: ' + error.response.data.message);
          })
        }
      })
    },
    containsKey(obj, key) {
      return Object.keys(obj).includes(key);
    },
    roundNumber(number, decimals) {
      return number.toFixed(decimals);
    },
    roundSensitiveNumber(number, decimals) {
      if (!this.showAmount) {
        return "******";
      }
      return this.roundNumber(number, decimals);
    },
  },
  mounted() {
    this.getGlobalStats();
    this.getToggle();
    this.getToggleData();
    this.getShowAmount();
    this.getShowPercentage();
  }
}
</script>

<style scoped>
</style>