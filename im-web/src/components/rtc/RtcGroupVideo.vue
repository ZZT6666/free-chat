<template>
	<el-dialog v-dialogDrag top="5vh" title="群组语音通话" :close-on-click-modal="false" :close-on-press-escape="false"
		:visible.sync="isShow" width="50%">
		<div class="rtc-group-video">
			<div class="rtc-voice-box" v-loading="!isChating" element-loading-text="等待其他成员加入..."
				element-loading-background="rgba(0, 0, 0, 0.1)">
				<div class="member-list">
					<div v-for="member in members" :key="member.userId" class="member-item">
						<head-image :id="member.userId" :size="80" :name="member.nickName" :url="member.headImage" :isShowUserInfo="false">
							<div class="member-name">{{ member.nickName }}</div>
						</head-image>
					</div>
				</div>
			</div>
			<div class="rtc-control-bar">
				<div title="挂断" class="icon iconfont icon-phone-reject reject" style="color: red;" @click="onQuit()"></div>
			</div>
		</div>
	</el-dialog>
</template>

<script>
import HeadImage from '../common/HeadImage.vue';
import ImWebRtc from '@/api/webrtc';
import ImCamera from '@/api/camera';
import RtcGroupApi from '@/api/rtcGroupApi';

export default {
	name: "rtcGroupVideo",
	components: {
		HeadImage
	},
	data() {
		return {
			isShow: false,
			groupId: '',
			members: {}, // 群组成员列表
			peerConnections: new Map(), // 存储与每个成员的WebRTC连接
			camera: new ImCamera(),
			API: new RtcGroupApi(),
			state: 'CLOSE', // CLOSE:关闭 WAITING:等待 CHATING:通话中
			localStream: null,
			heartbeatTimer: null,
			isHost: false
		}
	},
	computed: {
		isChating() {
			return this.state === 'CHATING';
		}
	},
	methods: {
		open(rtcInfo) {
			this.isShow = true;
			this.groupId = rtcInfo.groupId;
			this.members = rtcInfo.userInfos;
			this.isHost = rtcInfo.isHost;

			if (this.isHost) {
				this.setupCall();
			}
		},

		async setupCall() {
			if (!this.checkDevEnable()) {
				this.close();
				return;
			}

			try {
				// 打开麦克风
				this.localStream = await this.camera.openAudio();

				// 初始化所有成员的WebRTC连接
				for (let member of this.members) {
					if (member.userId !== this.$store.state.userStore.userId) {
						this.setupPeerConnection(member.userId);
					}
				}

				// 发起群组通话
				await this.API.setup(this.groupId, this.members);
				this.state = 'WAITING';
				this.startHeartBeat();

			} catch (error) {
				console.error('Setup call failed:', error);
				this.close();
			}
		},

		setupPeerConnection(userId) {
			const webrtc = new ImWebRtc();
			webrtc.init(this.$store.state.configStore.webrtc);
			webrtc.setupPeerConnection((stream) => {
				// 处理远程流
				console.log('Received remote stream from:', userId);
			});

			// 设置本地流
			if (this.localStream) {
				webrtc.setStream(this.localStream);
			}

			// 处理ICE候选
			webrtc.onIcecandidate((candidate) => {
				this.API.candidate(this.groupId, userId, candidate);
			});

			this.peerConnections.set(userId, webrtc);
		},

		onRTCMessage(msg) {
			if (this.state === 'CLOSE' && msg.type !== this.$enums.MESSAGE_TYPE.RTC_GROUP_INVITE) {
				return;
			}

			switch (msg.type) {
				case this.$enums.MESSAGE_TYPE.RTC_GROUP_INVITE:
					this.handleSetup(msg);
					break;
				case this.$enums.MESSAGE_TYPE.RTC_GROUP_OFFER:
					this.handleOffer(msg);
					break;
				case this.$enums.MESSAGE_TYPE.RTC_GROUP_ANSWER:
					this.handleAnswer(msg);
					break;
				case this.$enums.MESSAGE_TYPE.RTC_GROUP_CANDIDATE:
					this.handleCandidate(msg);
					break;
				case this.$enums.MESSAGE_TYPE.RTC_GROUP_QUIT:
					this.handleQuit(msg);
					break;
			}
		},

		startHeartBeat() {
			this.heartbeatTimer = setInterval(() => {
				this.API.heartbeat(this.groupId);
			}, 15000);
		},

		checkDevEnable() {
			if (!this.camera.isEnable()) {
				this.$message.error("访问麦克风失败");
				return false;
			}
			return true;
		},

		onQuit() {
			this.API.quit(this.groupId);
			this.close();
		},

		close() {
			this.isShow = false;
			this.camera.close();
			this.peerConnections.forEach(webrtc => webrtc.close());
			this.peerConnections.clear();
			this.heartbeatTimer && clearInterval(this.heartbeatTimer);
			this.state = 'CLOSE';
		},

		async handleSetup(msg) {

			if (!msg.selfSend) {
				console.log('非发起人，显示加入对话框');
				// 非发起人，显示加入对话框
				await this.$http({
				url: `/user/find/${msg.sendId}`,
				method: 'get'
				}).then((user) => {
				this.host = user
				})

				await this.$http({
				url: '/user/self',
				method: 'get'
				}).then((user) => {
				this.user = [user]
				})

				console.log('host', this.host);
				console.log('res', this.user);
				let rtcInfo = {
				host: this.host,
				userInfos: this.user,
				groupId: msg.groupId,
				}

				this.$eventBus.$emit('showGroupJoin', rtcInfo);

			}

			// // 发起人逻辑
			// const userId = msg.sendId;
			// const pc = this.peerConnections.get(userId);
			// if (!pc) return;

			// try {
			// 	const offer = await pc.createOffer();
			// 	await pc.setLocalDescription(offer);
			// 	this.API.offer(this.groupId, userId, offer);
			// } catch (error) {
			// 	console.error('Create offer failed:', error);
			// }
		}
	},
	beforeUnmount() {
		this.onQuit();
	}
}
</script>

<style lang="scss">
.rtc-group-video {
	height: 400px;
	background-color: #E8F2FF;

	.rtc-voice-box {
		height: 320px;
		padding: 20px;

		.member-list {
			display: flex;
			flex-wrap: wrap;
			gap: 20px;
			justify-content: center;
		}

		.member-item {
			text-align: center;

			.member-name {
				margin-top: 8px;
				font-size: 14px;
			}
		}
	}

	.rtc-control-bar {
		display: flex;
		justify-content: center;
		padding: 10px;

		.icon {
			font-size: 40px;
			cursor: pointer;
		}
	}
}
</style>
