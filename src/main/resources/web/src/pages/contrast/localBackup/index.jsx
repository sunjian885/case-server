import React from 'react'
import { Table, Button, Card, Tooltip, message } from 'antd'
import request from '@/utils/axios'
import moment from 'moment'
import './index.scss'

class LocalBackup extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      rowKeys: [], // 当前选择行的 key
      rows: [], // 当前选择的行数据
      historyList: [],
    }
  }
  componentDidMount() {
    this.getLocalCacheList()
  }
  getLocalCacheList = () => {
    let cacheList = []
    let username = JSON.parse(localStorage.getItem('userinfo')).realName
    for (let i = 0; i < localStorage.length; i++) {
      if (localStorage.key(i).split('_')[0] == this.props.caseId) {
        let item = {
          id: localStorage.key(i),
          gmtCreated: localStorage.key(i).split('_')[1],
          creator: username,
        }
        cacheList.push(item)
      }
    }
    this.setState({ historyList: cacheList })
  }
  contrastClick = () => {
    for (let i = 0; i < localStorage.length; i++) {
      if (localStorage.key(i).split('_')[0] == this.props.caseId) {
        localStorage.removeItem(localStorage.key(i))
      }
    }
    this.getLocalCacheList()
  }

  restoreBackUpCase = item => {
    let key = item.id
    let caseId = key.split('_')[0]
    let jsonContent = localStorage.getItem(key)
    const param = {
      id: caseId,
      title: '更新内容，实际不会保存title',
      // recordId: 'undefine',
      modifier: JSON.parse(localStorage.getItem('userinfo')).realName,
      caseContent: jsonContent,
    }
    let url = `/case/update`
    request(url, { method: 'POST', body: param }).then(res => {
      if (res.code == 200) {
        //保存成功，删除内容
        message.success('保存内容成功')
        localStorage.removeItem(key)
        this.getLocalCacheList()
      } else {
        message.error(res.msg)
      }
    })
  }

  setTableColums = () => {
    const columns = [
      {
        title: '备份Key',
        dataIndex: 'id',
      },
      {
        title: '创建时间',
        dataIndex: 'gmtCreated',
        render: text => {
          return <span>{moment(text, 'YYYYMMDDhhmmss').format('YYYY-MM-DD HH:mm:ss')}</span>
        },
      },
      {
        title: '创建人',
        dataIndex: 'creator',
      },
      {
        title: '操作',
        key: 'operation',
        render: item => {
          return (
            <Button
              type="primary"
              onClick={() => {
                //todo 恢复用例
                this.restoreBackUpCase(item)
              }}
            >
              恢复用例
            </Button>
          )
        },
      },
    ]
    return columns
  }
  render() {
    // const rowSelection = {
    //   onChange: (selectedRowKeys, selectedRows) => {
    //     this.setState({ rowKeys: selectedRowKeys, rows: selectedRows })
    //   },
    //   getCheckboxProps: record => ({
    //     disabled: this.state.rowKeys.length >= 2 && !this.state.rowKeys.includes(record.id),
    //     name: record.name,
    //   }),
    // }
    return (
      <Card
        bordered={false}
        className={this.state.rowKeys.length >= 2 ? 'contras_card' : 'contras_card_default'}
      >
        <div className="contras_title">
          {/* <span>历史版本</span> */}
          <Tooltip placement="top" title="删除本地所有关于本用例的缓存">
            <Button
              type="primary"
              //   disabled={this.state.rowKeys.length < 2}
              onClick={this.contrastClick}
            >
              删除所有
            </Button>
          </Tooltip>
        </div>
        <Table
          rowKey="id"
          // rowSelection={rowSelection}
          columns={this.setTableColums()}
          dataSource={this.state.historyList}
        />
      </Card>
    )
  }
}

export default LocalBackup
