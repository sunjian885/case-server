/** 历史版本对比 */
import React from 'react'
import { Layout, Menu } from 'antd'
import moment from 'moment'
import './index.scss'
import Headers from '../../layouts/headers'
import ServerBackup from './serverBackup/index'
import LocalBackup from './localBackup/index'
moment.locale('zh-cn')

const { Header } = Layout

class Contrast extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      buType: 'server',
    }
  }

  clickMenu(item){
    this.setState({
      buType: item.key,
    })
  }

  render() {
    return (
      <section style={{ marginBottom: 30 }}>
        <Headers />
        <Header className="header">
          {/* <div>功能选择</div> */}
          <Menu
            theme="dark"
            mode="horizontal"
            defaultSelectedKeys={['server']}
            style={{ lineHeight: '64px' }}
            onClick={item => this.clickMenu(item)}
          >
            <Menu.Item key="server">服务端</Menu.Item>
            <Menu.Item key="local">本地保存</Menu.Item>
          </Menu>
        </Header>
        {this.state.buType == 'server' && (
          <ServerBackup caseId={this.props.match.params.caseId} history={this.props.history} />
        )}
        {this.state.buType == 'local' && <LocalBackup caseId={this.props.match.params.caseId} />}
      </section>
    )
  }
}
export default Contrast
