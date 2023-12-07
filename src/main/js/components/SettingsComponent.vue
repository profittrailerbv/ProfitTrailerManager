<template>
  <div class="row">
    <div class="col font-weight-bold">
      <div class="card">
        <div class="card-body bg-light form-group">
          <div class="row">
            <div class="col">
              Global Settings
            </div>
          </div>
          <div class="row mt-3">
            <div class="col">
              <label for="tmz">Timezone:</label>
              <select v-model="settings.timezone" id="tmz" class="form-control">
                <option v-for="timezone in timezones" v-bind:value="timezone.id">
                  {{ timezone.value }}
                </option>
              </select>
            </div>
          </div>
          <div class="row mt-3">
            <div class="col">
              <label for="cur">Currency:</label>
              <select v-model="settings.currency" id="cur" class="form-control">
                <option v-for="currency in currencies" v-bind:value="currency.id">
                  {{ currency.id }}
                </option>
              </select>
            </div>
          </div>
          <div class="row mt-3">
            <div class="col">
              <label for="tok">Discord token:</label>
              <input id="tok" class="form-control" name="token" type="text" v-model="settings.token"
                     placeholder="">
            </div>
          </div>
          <div v-if="maxBots === 0 || maxBots === 999" class="row mt-3">
            <div class="col">
              <label for="tok">Reserve RAM for each bot:</label>
              <input id="ram" class="form-control" name="ram" type="text" v-model="settings.ram"
                     placeholder="">
            </div>
          </div>
          <div class="row mt-3">
            <div class="col-2">
              <label class ="" for="tok">Auto update bots:</label>
              <input id="autoUpdate" class="form-control" name="autoUpdate" type="checkbox" v-model="settings.autoUpdate"
                     placeholder="">
            </div>
            <div class="col">
              <label for="tok">Wait time between bot update:</label>
              <input id="updateDelay" class="form-control" name="updateDelay" type="text" v-model="settings.updateDelay"
                     placeholder="">
            </div>
          </div >
          <div class="row mt-4">
            <div class="col">
              <a v-if="!demoServer" href="#" @click.prevent="saveSettings()">
                <button type="button" class="btn btn-success form-control">Save</button>
              </a>
            </div>
          </div>
          <div class="row mt-2">
            <div class="col">
              <span v-if="error" class="text-soft-danger">{{ error }}</span>
            </div>
          </div>
          <div class="row mt-2">
            <div class="col">
              <span v-if="success" class="text-soft-success">{{ success }}</span>
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
      error: "",
      success: "",
      demoServer: false,
      maxBots: 0,
      timezones: [],
      currencies: [],
      settings: {
        timezone: "",
        currency: "",
        token: "",
        ram: "",
        autoUpdate:false,
      }
    }
  },
  methods: {
    getSettings() {
      axios.get('/api/v1/globalSettings').then((response) => {
        this.demoServer = response.data.demoServer;
        this.maxBots = response.data.maxBots;
        this.timezones = response.data.timezones;
        this.currencies = response.data.currencies;
        this.settings.timezone = response.data.timezone;
        this.settings.currency = response.data.currency;
        this.settings.token = response.data.token;
        this.settings.ram = response.data.ram;
        this.settings.updateDelay = response.data.updateDelay;
        this.settings.autoUpdate = response.data.autoUpdate;
      }).catch((error) => {
        if (!error.response) {
          window.location.href = '/';
        } else {
          this.error = error.response.data.message;
        }
      })

    },
    saveSettings() {
      const formData = new FormData();
      formData.append("timezone", this.settings.timezone);
      formData.append("currency", this.settings.currency);
      formData.append("token", this.settings.token);
      formData.append("ram", this.settings.ram);
      formData.append("updateDelay", this.settings.updateDelay);
      formData.append("autoUpdate", this.settings.autoUpdate);

      axios.post('/api/v1/globalSettings', formData).then((response) => {
        this.success = "Settings were saved!";
      }).catch((error) => {
        this.error = error.response.data.message;
      })
    }
  },
  mounted() {
    this.getSettings()
  },
}
</script>

<style scoped>
.form-margin {
  margin-top: 20%;
}

.text-soft-danger {
  color: #be584a;
}
</style>