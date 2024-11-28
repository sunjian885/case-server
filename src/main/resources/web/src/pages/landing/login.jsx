import React from 'react'
import './less/login.less'
import { Form, message, Spin } from 'antd'
import request from '@/utils/axios'
import utils from '@/utils'
import qs from 'querystring'
import getQueryString from '@/utils/getCookies'

const getCookies = getQueryString.getCookie
class LogIn extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      type: '1', // 当前为什么类型 1：登录 2： 注册
      loading: false, // 点击注册登录
      regitserLoading: true, //注册的时候loading
      scene: '',
    }
  }

  componentDidMount() {
    let rolParam = null
    const params = qs.parse(this.props.location.search.substring(1))
    const { returnUrl, username, password, userid, realName } = params
    if (username && password && userid && realName) {
      rolParam = {
        username,
        password,
        userid,
        realName,
      }
    } else if (getCookies('current-user')) {
      const currentUser = JSON.parse(decodeURIComponent(getCookies('current-user')))
      rolParam = {
        username: currentUser.mobileNo,
        password: currentUser.mobileNo,
        userid: currentUser.operatorNo,
        realName: currentUser.name,
      }
    } else {
      rolParam = null
    }
    // eslint-disable-next-line no-console
    if (rolParam !== null && rolParam.userid !== null) {
      //发送登录请求，成功跳转returnUrl
      request(`/user/registerOrLogin`, {
        method: 'POST',
        body: rolParam,
      }).then(res => {
        if (res && res.code === 200) {
          message.success('登录登陆成功')
          // console.log('res.data ==', res.data)
          window.localStorage.setItem('userinfo', JSON.stringify(res.data))
          // 跳转到登录成功页面
          if (returnUrl) {
            window.location.href = returnUrl
          } else {
            window.location.href = 'http://' + window.location.host + '/case/caseList/1'
          }
        } else {
          message.error(res.msg)
          // 跳转到登录页面
          window.location.href = 'https://h5.test.shantaijk.cn/stsso/#/user/login?appId=CASE_SERVER'
        }
      })
    }
  }

  render() {
    const loadingStyle = {
      position: 'absolute',
      top: '0',
      left: '0',
      right: '0',
      bottom: '0',
      background: 'rgba(250, 250, 250, 0.65)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      // zIndex: '9999',
      fontSize: '20px',
    }
    const { regitserLoading } = this.state

    return (
      <div className="login">
        <div className="load" style={loadingStyle} size="large">
          <Spin spinning={regitserLoading} delay={500} tip="登录中......"></Spin>
        </div>
      </div>
    )
  }
}

export default Form.create()(LogIn)
