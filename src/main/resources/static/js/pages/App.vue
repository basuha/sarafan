<template>
    <v-app>
        <v-app-bar app max-height="60">
            <v-toolbar-title>Sarafan</v-toolbar-title>
            <v-btn text
                   v-if="profile"
                   :disabled="$route.path === '/'"
                   @click="showMessages">
                Messages
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn text
                   v-if="profile"
                   :disabled="$route.path === '/profile'"
                   @click="showProfile">
                {{profile.name}}
            </v-btn>
            <v-btn v-if="profile" icon href="/logout">
                <v-icon>exit_to_app</v-icon>
            </v-btn>
        </v-app-bar>
        <v-main>
            <router-view></router-view>
        </v-main>
    </v-app>
</template>

<script>
    import {mapState, mapMutations} from 'vuex'
    import {addHandler} from "util/ws";

    export default {
        computed: mapState(['profile']),
        methods: {
            ...mapMutations(['addMessageMutation', 'updateMessageMutation', 'removeMessageMutation']),
            showMessages() {
                this.$router.push('/')
            },
            showProfile() {
                this.$router.push('/profile')
            }
        },
        created() {
            addHandler(data => {
                if (data.objectType === 'MESSAGE') {
                    switch (data.eventType) {
                        case 'CREATE':
                            this.addMessageMutation(data.body)
                            break
                        case 'UPDATE':
                            this.updateMessageMutation(data.body)
                            break
                        case 'REMOVE':
                            this.removeMessageMutation(data.body)
                            break
                        default:
                            console.error('Event type is unknown ${data.eventType}')
                    }
                } else {
                    console.error('object type is unknown ${data.objectType}')
                }
            })
        },
        beforeMount() {
            if(!this.profile) {
                this.$router.replace('/auth')
            }
        }
    }
</script>

<style>
</style>
