<template>
  <div class="bot-component row row-cols-1 row-cols-md-2 row-cols-lg-3 row-cols-xxl-4">
    <div class="col mb-4" v-for="addon in addons">
      <div class="card">
        <div class="card-body bg-light">
          <div class="row text-muted">
            <div class="col-10 text-left font-weight-bold pr-0">
              <font-awesome-icon :class="getStatusClass(addon.status, addon.initialSetup)"
                                 :icon="['fas','circle']"></font-awesome-icon>
              {{ addon.siteName }}
              <span v-if="addon.managed && containsKey(addon.data, 'version')"
                    class="smaller"><sup>V{{ addon.data.version }}</sup></span>
            </div>
          </div>
        </div>
        <div style="border-width:1px" class="card-footer border-dark"
             :class="addon.data.totalProfitAllTime > 0 ? 'bg-soft-green' : addon.data.totalProfitAllTime < 0 ? 'bg-soft-red' : ''">
          <div class="row">
            <div class="col-6 text-left">
<!--              C: {{bot.cpu}}-->
<!--              R: {{roundNumber(bot.ram/1000,0)}}-->
            </div>
            <div class="col-6 text-right"lo>
              <a v-if="!demoServer && addon.managed" href="#"
                 @click.prevent="restartBot(addon.directory, addon.siteName)">
                <font-awesome-icon :icon="['fas','redo-alt']" class="text-dark"></font-awesome-icon>
              </a>
              <a v-if="!demoServer && addon.managed" href="#"
                 @click.prevent="stopBot(addon.directory, addon.siteName)">
                <font-awesome-icon :icon="['fas','stop-circle']" class="text-dark"></font-awesome-icon>
              </a>
              <a v-if="!demoServer && !addon.managed" href="#"
                 @click.prevent="manage(addon.directory, addon.siteName)">
                <button type="button" class="btn btn-primary small">Manage</button>
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
      addons: [],
      loading: true,
      timer: '',
      demoServer: false,
      downloadUrl: '',
      maxReached: false
    }
  },
  created() {
    this.timer = setInterval(this.getAllData, 5000)
  },
  methods: {
    getAllData() {
      axios.get('/api/v1/addon/data').then((response) => {
        this.addons = response.data.addons
        this.demoServer = response.data.demoServer
        this.downloadUrl = response.data.downloadUrl
        this.maxReached = response.data.maxReached
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
          axios.post('/api/v1/addon/restart?directoryName=' + name).then(() => {
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
          axios.post('/api/v1/addon/stopAndUnlink?directoryName=' + name).then(() => {
            this.$swal.fire('The restart command has been sent...');
            this.getBotStatus(name);
          }).catch((error) => {
            this.$swal.fire(error.response.data.message);
          })
        }
      })
    },
    manage(name, siteName) {
      this.$swal.fire({
        title: 'Confirm manage',
        text: 'Manage this bot ' + siteName + '? It will be restarted.',
        showCancelButton: true,
        cancelButtonText: 'Exit',
        confirmButtonText: 'Manage',
        reverseButtons: true
      }).then((result) => {
        if (typeof (result.value) !== 'undefined') {
          axios.post('/api/v1/addon/restart?directoryName=' + name).then(() => {
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
      axios.get('/api/v1/addon/status?directoryName=' + name).then(() => {
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
    }
  },
  mounted() {
    this.getAllData()
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