import React from 'react'
import 'antd/dist/antd.css'
import { Layout, Icon, Menu, Dropdown, message, Button } from 'antd'
import getQueryString from '@/utils/getCookies'
import '../pages/landing/less/index.less'
import request from '@/utils/axios'
const { Header } = Layout
const getCookies = getQueryString.getCookie

class Headers extends React.Component {
  componentDidMount() {
    if (!getCookies('username')) {
      window.location.href = `https://h5.test.shantaijk.cn/stsso/#/user/login?appId=CASE_SERVER`
    }
  }

  toAIGCLab = ()=>{
    window.location.href='/ai'
  }

  // 登出
  handleDropdownClick = () => {
    request(`/user/quit`, {
      method: 'POST',
    }).then(res => {
      // 清除本地缓存
      if (res && res.code === 200) {
        window.location.href = `https://h5.test.shantaijk.cn/stsso/#/user/login?appId=CASE_SERVER`
      } else {
        message.error(res.msg)
      }
    })
  }

  render() {
    const menu = (
      <Menu className="menu" onClick={this.handleDropdownClick}>
        <Menu.Item key="logout">
          <span>
            <Icon type="logout" />
            退出登录
          </span>
        </Menu.Item>
      </Menu>
    )
    return getCookies('username') ? (
      <Header style={{ zIndex: 9, display: 'flex', justifyContent:'space-between' }}>
        <a href="/case/caseList/1" style={{ color: '#fff', fontSize: 24 }}>
          杉泰脑图
        </a>
        <div>
          <Button type="primary" style={{marginRight:15}} onClick={()=> this.toAIGCLab()}>AIGC实验室 <Icon type="right" /></Button>
          <Dropdown overlay={menu} overlayClassName="dropStyle" placement="bottomLeft">
            <div className="user">
              <Icon type="user" className="userIcon" />
              <span className="username">{getCookies('username')}</span>
              <Icon type="down" className="dowm" />
            </div>
          </Dropdown>
        </div>
        
      </Header>
    ) : null
  }
}
export default Headers
