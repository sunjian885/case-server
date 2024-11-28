/* eslint-disable no-console */
import React from 'react'
import PropTypes from 'prop-types'
import io from './assets/socketio/socket.io.js'
import { notification } from 'antd'
import { filterNodeFromMinder, progressApplyPatch } from './util/filterProgress'
import jsonDiff from 'fast-json-patch'

// import { AsyncStorage } from 'react-native-community/async-storage';

class Socket extends React.Component {
  constructor(props) {
    super(props)
    this.state = { ws: io(this.props.url, props.wsParam) }
    this.sendMessage = this.sendMessage.bind(this)
    this.setupSocket = this.setupSocket.bind(this)
    this.leaveListener = this.leaveListener.bind(this)
  }

  setupSocket() {
    let websocket = this.state.ws

    websocket.on('connect', () => {
      if (typeof this.props.onOpen === 'function') this.props.onOpen()
    })

    websocket.on('reconnect', () => {
      console.log(this.props)
      websocket.disconnect()
      notification.error({ message: 'Version of client is not equal to server, please refresh.' })
    })

    websocket.on('disconnect', () => {
      if (typeof this.props.onClose === 'function') this.props.onClose()
      //清理localStorage中无用的存储
      for (let i = 0; i < localStorage.length; i++) {
        let key = localStorage.key(i) //获取本地存储的Key
        if (key.includes('xhr-polling')) {
          localStorage.removeItem(key)
        }
      }
      localStorage.setItem(
        JSON.stringify(this.props.wsParam),
        JSON.stringify(this.props.wsMinder.exportJson()),
      )
    })

    websocket.on('connect_notify_event', evt => {
      console.log('connect notify ', evt.message)
      if (typeof this.props.handleWsUserStat === 'function')
        this.props.handleWsUserStat(evt.message)
    })

    //修改不读本地缓存，直接读取数据
    websocket.on('open_event', evt => {
      const recv = JSON.parse(evt.message || '{}')
      const dataJson = { ...recv }
      // console.log('open_event dataJson ==', dataJson)
      if (evt.message === JSON.stringify(this.props.wsMinder.exportJson())) {
        return
      }
      window.minderData = undefined
      // eslint-disable-next-line prettier/prettier
      // console.log('websocket.on(open_event, evt => {  exception' + '  work ')
      this.props.wsMinder.importJson(dataJson)
      window.minderData = dataJson
      if (this.props.iscore === '3') {
        let sessionKey = 'originminder_' + this.props.wsParam.query.recordId
        sessionStorage.setItem(sessionKey, JSON.stringify(dataJson))
      }
      // 第一次打开用例，预期base与用例的base保持一直
      this.expectedBase = this.props.wsMinder.getBase()
    })
    /**
    websocket.on('open_event', evt => {
      const recv = JSON.parse(evt.message || '{}')
      const dataJson = { ...recv }
      // console.log('open_event dataJson ==', dataJson)
      try {
        const cacheContent = JSON.parse(localStorage.getItem(JSON.stringify(this.props.wsParam)))
        console.log('cacheContent===', cacheContent)
        if (cacheContent == undefined) {
          throw 'cache is empty'
        }
        console.log('dataJson.base ==', dataJson.base)
        console.log('cacheContent.base ==', cacheContent.base)
        console.log('dataJson ==', dataJson)
        console.log('cacheContent ==', cacheContent)
        if (dataJson.base > cacheContent.base) {
          // 服务端版本高
          throw 'choose server'
        } else {
          // 客户端版本高 或者 相同
          // do nothing
          // websocket.sendMessage('edit', {
          //   caseContent: JSON.stringify(cacheContent),
          //   patch: null,
          //   caseVersion: caseContent.base,
          // })
        }
        window.minderData = undefined
        console.log('websocket.on(open_event, evt => {' + '  work ')
        this.props.wsMinder.importJson(cacheContent)
        window.minderData = cacheContent
        this.expectedBase = this.props.wsMinder.getBase()
        console.log('import case from cache. cache base: ', cacheContent.base)
        // todo 测试版本，暂不清除
        localStorage.removeItem(JSON.stringify(this.props.wsParam))
      } catch (e) {
        console.error(e)

        // console.log('接收消息，data: ', evt.message)
        // console.log('接收消息，当前内容: ', JSON.stringify(this.props.wsMinder.exportJson()))
        if (evt.message === JSON.stringify(this.props.wsMinder.exportJson())) {
          return
        }

        window.minderData = undefined
        // eslint-disable-next-line prettier/prettier
        // console.log('websocket.on(open_event, evt => {  exception' + '  work ')
        this.props.wsMinder.importJson(dataJson)
        window.minderData = dataJson

        // 第一次打开用例，预期base与用例的base保持一直
        this.expectedBase = this.props.wsMinder.getBase()
        console.log('----- 接收消息，expected base: ', this.expectedBase)
      }
    })
 */

    websocket.on('edit_ack_event', evt => {
      // console.log('edit_ack_event', evt.message)
      // console.log('edit_ack_event props ', this.props)
      const recv = JSON.parse(evt.message || '{}')
      // 如果json解析没有root节点
      this.props.wsMinder.setStatus('readonly')
      const recvPatches = this.travere(recv)
      // const recvBase = recvPatches.filter((item) => item.path === '/base')[0]?.value;
      // const recvFromBase = recvPatches.filter((item) => item.path === '/base')[0]?.fromValue;
      try {
        if (this.props.wsParam.query.recordId != 'undefine') {
          let sessionKey = 'originminder_' + this.props.wsParam.query.recordId
          progressApplyPatch(sessionKey, recvPatches)
        }
      } catch (e) {
        alert('edit_ack_event 客户端接受应答消息异常，请刷新重试')
      }
      this.props.wsMinder.applyPatches(recvPatches)
      this.props.wsMinder._status = 'normal'
    })

    websocket.on('edit_notify_event', evt => {
      const recv = JSON.parse(evt.message || '{}')
      // console.log('recv ==', recv)
      // 如果json解析没有root节点
      try {
        this.props.wsMinder._status = 'readonly'
        const recvPatches = this.travere(recv)
        //如果发现是实验室模式的情况下，就将内容更新到sessionStorage中
        //然后再次根据内容筛选内容过滤
        //再次跟现在minder中的内容做diff，将diff内容通过minder的applyPatches方法，写入到minder view中
        if (this.props.iscore === '3') {
          let sessionKey = 'originminder_' + this.props.wsParam.query.recordId
          let newCaseContent = progressApplyPatch(sessionKey, recvPatches)
          // console.log('edit_notify_event newCaseContent ==',newCaseContent)
          if (this.props.progressFilterValue.length == 0) {
            this.props.wsMinder.applyPatches(recvPatches)
          } else {
            //将最新内容filter一下，就是最终页面要展示的内容
            let filterValue =
              this.props.progressFilterValue == null ? [99] : this.props.progressFilterValue
            let filterContent = filterValue.includes(100)
              ? newCaseContent
              : filterNodeFromMinder(newCaseContent, this.props.wsMinder.exportJson())
            // console.log('edit_notify_event filterContent==', filterContent)
            // console.log('this.props.wsMinder.exportJson()==', this.props.wsMinder.exportJson())
            //diff一下最终要展示的内容
            let diffValue = jsonDiff.compare(this.props.wsMinder.exportJson(), filterContent)
            // console.log('edit_notify_event diffValue==', diffValue)

            // const diffRecvPatches = this.travere(diffValue)
            // console.log('edit_notify_event diffRecvPatches==', diffRecvPatches)

            this.props.wsMinder.applyPatches(diffValue)
          }
        } else {
          this.props.wsMinder.applyPatches(recvPatches)
        }
      } catch (e) {
        console.info('edit_notify_event 异常 e', e)
        alert(' edit_notify_event 客户端接受通知消息异常，请刷新重试')
      } finally {
        //更改更新后会发送通知
        this.props.wsMinder._status = 'normal'
      }
      this.props.wsMinder._status = 'normal'
    })

    // message 0:加锁；1：解锁；2:加/解锁成功；3:加/解锁失败
    websocket.on('lock', evt => {
      console.log('lock info', evt.message)
      if (typeof this.props.handleLock === 'function') this.props.handleLock(evt.message)
    })

    websocket.on('connect_error', e => {
      console.log('connect_error', e)
      websocket.disconnect()
    })

    websocket.on('warning', e => {
      notification.error({ message: 'server process patch failed, please refresh' })
    })
  }

  travere = arrPatches => {
    let patches = []
    for (let i = 0; i < arrPatches.length; i++) {
      if (arrPatches[i].op === undefined) {
        for (let j = 0; j < arrPatches[i].length; j++) {
          patches.push(arrPatches[i][j])
        }
      } else {
        patches.push(arrPatches[i])
      }
    }
    return patches
  }

  //发送ws消息
  sendMessage(type, message) {
    let websocket = this.state.ws
    // console.log('-- message --', message)
    // var jsonObject = {userName: 'userName', message: message};
    websocket.emit(type, message)
  }

  leaveListener(e) {
    e.preventDefault()
    e.returnValue = '内容将被存储到缓存，下次打开相同用例优先从缓存获取！'
    if (this.props.wsMinder.getBase() > 16) {
      for (let i = 0; i < localStorage.length; i++) {
        let key = localStorage.key(i) //获取本地存储的Key
        if (key.includes('xhr-polling')) {
          localStorage.removeItem(key)
        }
      }
      localStorage.setItem(
        JSON.stringify(this.props.wsParam),
        JSON.stringify(this.props.wsMinder.exportJson()),
      )
    }
  }

  componentDidMount() {
    console.log(' -- componentDidMount -- ')
    this.setupSocket()
    window.addEventListener('beforeunload', this.leaveListener)
  }

  componentWillUnmount() {
    window.removeEventListener('beforeunload', this.leaveListener)

    this.state.ws.disconnect()
    console.log(' -- componentWillUnmount -- ')
  }

  render() {
    return <div></div>
  }
}

Socket.propTypes = {
  url: PropTypes.string.isRequired,
  onMessage: PropTypes.func.isRequired,
  onOpen: PropTypes.func,
  onClose: PropTypes.func,
  handleLock: PropTypes.func,
  handleWsUserStat: PropTypes.func,
}

export default Socket
