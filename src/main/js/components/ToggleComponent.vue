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
            <span v-if="latestVersion !== '0.0.0'" class="d-none d-sm-inline">Github latest: {{ latestVersion }}</span>
            <a v-if="!demoServer" href="#" @click.prevent="updateVersion()" class="text-left">
              <font-awesome-icon class="text-danger" :icon="['fas','level-up-alt']"></font-awesome-icon>
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
              <span v-if="containsKey(globalStats, 'totalProfitTodayLive')"
                    :class="globalStats.totalProfitTodayLive > 0 ? 'text-soft-success' : globalStats.totalProfitTodayLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundNumber(globalStats.totalProfitTodayLive, 2) }} {{ globalStats.currency }}
              </span>
                <span v-if="containsKey(globalStats, 'totalProfitTodayLive') && globalStats.totalTCVLive > 0"
                      :class="globalStats.totalProfitTodayLive > 0 ? 'text-soft-success' : globalStats.totalProfitTodayLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalProfitTodayLive / globalStats.totalTCVLive * 100, 2) }}%)
              </span>
              </div>
            </div>
            <div class="row text-soft-dark">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">Today (T)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalProfitTodayTest')"
                    :class="globalStats.totalProfitTodayTest > 0 ? 'text-soft-success' : globalStats.totalProfitTodayTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundNumber(globalStats.totalProfitTodayTest, 2) }} {{ globalStats.currency }}
              </span>
                <span v-if="containsKey(globalStats, 'totalProfitTodayTest') && globalStats.totalTCVTest > 0"
                      :class="globalStats.totalProfitTodayTest > 0 ? 'text-soft-success' : globalStats.totalProfitTodayTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalProfitTodayTest / globalStats.totalTCVTest * 100, 2) }}%)
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
              <span v-if="containsKey(globalStats, 'totalProfitThisMonthLive')"
                    :class="globalStats.totalProfitThisMonthLive > 0 ? 'text-soft-success' : globalStats.totalProfitThisMonthLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundNumber(globalStats.totalProfitThisMonthLive, 2) }} {{ globalStats.currency }}
              </span>
                <span v-if="containsKey(globalStats, 'totalProfitThisMonthLive') && globalStats.totalTCVLive > 0"
                      :class="globalStats.totalProfitThisMonthLive > 0 ? 'text-soft-success' : globalStats.totalProfitThisMonthLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalProfitThisMonthLive / globalStats.totalTCVLive * 100, 2) }}%)
              </span>
              </div>
            </div>
            <div class="row text-soft-dark">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">Month (T)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalProfitThisMonthTest')"
                    :class="globalStats.totalProfitThisMonthTest > 0 ? 'text-soft-success' : globalStats.totalProfitThisMonthTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundNumber(globalStats.totalProfitThisMonthTest, 2) }} {{ globalStats.currency }}
              </span>
                <span v-if="containsKey(globalStats, 'totalProfitThisMonthTest') && globalStats.totalTCVTest > 0"
                      :class="globalStats.totalProfitThisMonthTest > 0 ? 'text-soft-success' : globalStats.totalProfitThisMonthTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalProfitThisMonthTest / globalStats.totalTCVTest * 100, 2) }}%)
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
              <span v-if="containsKey(globalStats, 'totalProfitLastMonthLive')"
                    :class="globalStats.totalProfitLastMonthLive > 0 ? 'text-soft-success' : globalStats.totalProfitLastMonthLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundNumber(globalStats.totalProfitLastMonthLive, 2) }} {{ globalStats.currency }}
              </span>
                <span v-if="containsKey(globalStats, 'totalProfitLastMonthLive') && globalStats.totalTCVLive > 0"
                      :class="globalStats.totalProfitLastMonthLive > 0 ? 'text-soft-success' : globalStats.totalProfitLastMonthLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalProfitLastMonthLive / globalStats.totalTCVLive * 100, 2) }}%)
              </span>
              </div>
            </div>
            <div class="row text-soft-dark">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">P-Month (T)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalProfitLastMonthTest')"
                    :class="globalStats.totalProfitLastMonthTest > 0 ? 'text-soft-success' : globalStats.totalProfitLastMonthTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundNumber(globalStats.totalProfitLastMonthTest, 2) }} {{ globalStats.currency }}
              </span>
                <span v-if="containsKey(globalStats, 'totalProfitLastMonthTest') && globalStats.totalTCVTest > 0"
                      :class="globalStats.totalProfitLastMonthTest > 0 ? 'text-soft-success' : globalStats.totalProfitLastMonthTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalProfitLastMonthTest / globalStats.totalTCVTest * 100, 2) }}%)
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
              <span v-if="containsKey(globalStats, 'totalProfitAllTimeLive')"
                    :class="globalStats.totalProfitAllTimeLive > 0 ? 'text-soft-success' : globalStats.totalProfitAllTimeLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundNumber(globalStats.totalProfitAllTimeLive, 2) }} {{ globalStats.currency }}
              </span>
                <span v-if="containsKey(globalStats, 'totalProfitAllTimeLive') && globalStats.totalTCVLive > 0"
                      :class="globalStats.totalProfitAllTimeLive > 0 ? 'text-soft-success' : globalStats.totalProfitAllTimeLive < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalProfitAllTimeLive / globalStats.totalTCVLive * 100, 2) }}%)
              </span>
              </div>
            </div>
            <div class="row text-soft-dark">
              <div class="col-xs-12 col-md-4 pl-0 text-left font-weight-bold">
                <span class="">All (T)</span><br/>
              </div>
              <div class="col-xs-12 col-md-8 pl-0 text-left font-weight-bold">
              <span v-if="containsKey(globalStats, 'totalProfitAllTimeTest')"
                    :class="globalStats.totalProfitAllTimeTest > 0 ? 'text-soft-success' : globalStats.totalProfitAllTimeTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                {{ roundNumber(globalStats.totalProfitAllTimeTest, 2) }} {{ globalStats.currency }}
              </span>
                <span v-if="containsKey(globalStats, 'totalProfitAllTimeTest') && globalStats.totalTCVTest > 0"
                      :class="globalStats.totalProfitAllTimeTest > 0 ? 'text-soft-success' : globalStats.totalProfitAllTimeTest < 0 ? 'text-soft-danger' : 'text-soft-dark'">
                ({{ roundNumber(globalStats.totalProfitAllTimeTest / globalStats.totalTCVTest * 100, 2) }}%)
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
    containsKey(obj, key) {
      return Object.keys(obj).includes(key);
    },
    roundNumber(number, decimals) {
      return number.toFixed(decimals);
    },
  },
  mounted() {
    this.getGlobalStats();
    this.getToggle();
    this.getToggleData();
  }
}
</script>

<style scoped>
</style>