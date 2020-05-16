<template>
    <div class="row row-cols-1 row-cols-md-3">
        <div class="col mb-4" v-for="bot in bots">
            {{ bot }}
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">{{ bot.name }}</h5>
                    <p>{{ bot.status }}</p>
                    <a href="#" @click.prevent="startBot(bot.name)">Start Bot</a>
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
            startBot(name) {
                axios.post('/api/v1/startBot?botName=' + name).then((response) => {
                    console.log(response)
                }).catch((error) => {
                    console.log(error)
                })
            },
            getBotStatus(name) {
                axios.get('/api/v1/status?botName=' + name).then((response) => {
                    console.log(response)
                }).catch((error) => {
                    console.log(error)
                })
            }
        },
        mounted() {
            this.getAllBots()
        }
    }
</script>

<style scoped>

</style>