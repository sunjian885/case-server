import React from 'react'
import { Table, Button, Card, Tooltip } from 'antd'
import request from '@/utils/axios'
import moment from 'moment'
import './index.scss'

class ServerBackup extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      rowKeys: [], // 当前选择行的 key
      rows: [], // 当前选择的行数据
      historyList: [],
    }
  }
  componentDidMount() {
    this.getHistoryList()
  }
  getHistoryList = () => {
    request(`/backup/getBackupByCaseId`, {
      method: 'GET',
      params: {
        caseId: this.props.caseId,
      },
    }).then(res => {
      if (res.code === 200) {
        this.setState({ historyList: res.data })
      }
    })
  }
  contrastClick = () => {
    const { rows } = this.state
    this.props.history.push(`/caseManager/historyContrast/${rows[0].id}/${rows[1].id}`)
  }
  setTableColums = () => {
    const columns = [
      {
        title: '备份ID',
        dataIndex: 'id',
      },
      {
        title: '创建时间',
        dataIndex: 'gmtCreated',
        render: text => {
          return <span>{moment(text).format('YYYY-MM-DD HH:mm:ss')}</span>
        },
      },
      {
        title: '创建人',
        dataIndex: 'creator',
      },
      {
        title: '操作',
        key: 'operation',
        render: record => {
          return (
            <Button
              type="primary"
              onClick={() => {
                this.props.history.push('/caseview?backupId=' + record.id)
              }}
            >
              查看用例
            </Button>
          )
        },
      },
    ]
    return columns
  }
  render() {
    const rowSelection = {
      onChange: (selectedRowKeys, selectedRows) => {
        this.setState({ rowKeys: selectedRowKeys, rows: selectedRows })
      },
      getCheckboxProps: record => ({
        disabled: this.state.rowKeys.length >= 2 && !this.state.rowKeys.includes(record.id),
        name: record.name,
      }),
    }
    return (
      <Card
        bordered={false}
        className={this.state.rowKeys.length >= 2 ? 'contras_card' : 'contras_card_default'}
      >
        <div className="contras_title">
          {/* <span>历史版本</span> */}
          <Tooltip
            placement="top"
            title={this.state.rowKeys.length < 2 ? '选择两个版本后，才可以对比哦～' : null}
          >
            <Button
              type="primary"
              disabled={this.state.rowKeys.length < 2}
              onClick={this.contrastClick}
            >
              对比已选择版本
            </Button>
          </Tooltip>
        </div>
        <Table
          rowKey="id"
          rowSelection={rowSelection}
          columns={this.setTableColums()}
          dataSource={this.state.historyList}
        />
      </Card>
    )
  }
}

export default ServerBackup
