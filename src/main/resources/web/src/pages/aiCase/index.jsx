import React from 'react'
import Headers from '../../layouts/headers'
import { Layout, Menu, Icon } from 'antd'
import { Link } from "react-router-dom"

import withRouter from 'umi/withRouter'

const { Content, Sider } = Layout
const { SubMenu } = Menu

class AICase extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      rowKeys: [], // 当前选择行的 key
      rows: [], // 当前选择的行数据
      historyList: [],
    }
  }

  render() {
    const { children = {} } = this.props
    const menuList = [
      {
        title: 'AIGC实验室',
        key: 'AIService',
        icon: <Icon type="smile" theme="twoTone" />,
        children: [
          {
            title: 'AIGC生成',
            key: 'generateCases',
            icon: <Icon type="like" theme="twoTone" />,
            path: '/ai/generateCase',
          },
          {
            title: '历史结果查询',
            key: 'queryAICases',
            icon: <Icon type="database" theme="twoTone" />,
            path: '/ai/queryAICases',
          },
        ],
      },
    ]
    return (
      <Layout>
        <Headers />
        <Layout>
          <Sider width={200} style={{ background: '#fff' }}>
            <Menu mode="inline" style={{ height: '100%' }} defaultOpenKeys={['AIService']}>
              {menuList.map(item => {
                return (
                  <SubMenu
                    key={item.key}
                    title={
                      <span>
                        {item.icon}
                        {item.title}
                      </span>
                    }
                  >
                    {item.children.map(child =>{
                      return (
                        <Menu.Item key={child.key}>
                          <Link to={child.path}>
                            {child.icon}
                            {child.title}
                          </Link>
                        </Menu.Item>
                      )
                    })}
                  </SubMenu>
                )
              })}
            </Menu>
          </Sider>
          <Content style={{ minHeight: '100vh' }}>{children}</Content>
        </Layout>
      </Layout>
    )
  }
}

export default withRouter(AICase)
