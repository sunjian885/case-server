import React from 'react'
import { Spin, message } from 'antd'
import request from '@/utils/axios'
import qs from 'querystring'
// import utils from '@/utils'

class RegisterAndLogin extends React.PureComponent {
  constructor(props) {
    super(props)
    this.state = {
      loading: false, // 点击注册登录
    }
  }

  componentDidMount() {
    this.setState({ loading: true })
    const params = qs.parse(this.props.location.search.substring(1))
    // console.log('params ===',params)
    // console.log('params  params.get(returnUrl) ===',params.returnUrl)
    const returnUrl = params.returnUrl
    const rolParam = {
      username: params.username,
      password: params.password,
      userid: params.userid,
      realName: params.realName,
    }
    request(`/user/registerOrLogin`, {
      method: 'POST',
      body: rolParam,
    }).then(res => {
      if (res && res.code === 200) {
        message.success('登录登陆成功')
        // console.log('res.data ==',res.data)
        window.localStorage.setItem('userinfo', JSON.stringify(res.data))
        // 跳转到登录成功页面
        // window.location.href = "http://case-in.test.shantaijk.cn/"
        if (returnUrl) {
          window.location.href = returnUrl
        } else {
          window.location.href = 'http://' + window.location.host
        }
      } else {
        message.error(res.msg)
        // 跳转到登录页面
        window.location.href = 'http://' + window.location.host + '/login'
      }
      this.setState({ loading: false })
    })
  }

  render() {
    // console.log('render')
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
      zIndex: '9999',
      fontSize: '20px',
    }
    const { loading } = this.state
    return (
      <div className="load" style={loadingStyle} size="large">
        <Spin spinning={loading} delay={500} tip="注册登录中..."></Spin>
      </div>
    )
  }
}

export default RegisterAndLogin
