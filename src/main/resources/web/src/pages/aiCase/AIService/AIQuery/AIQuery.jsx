import React from 'react'
import { Card, Col, Row, Input, Button, Table, Divider, Tag } from 'antd'
import request from '@/utils/axios'

const { Column } = Table

class AIQuery extends React.Component {
  constructor(props) {
    super(props)
    this.userNameRef = React.createRef()
    this.useridRef = React.createRef()
    this.idRef = React.createRef()
    this.state = {
      tableData: [],
      id: null,
      userid: null,
      username: '',
      pageSize: 10,
    }
  }

  componentDidMount() {
    request('/ai/getHistoryResults', {
      method: 'POST',
      body: {},
    }).then(res => {
      if (res.code === 200) {
        this.setState({ tableData: res.data })
      }
    })
  }

  queryHistory = () => {
    request('/ai/getHistoryResults', {
      method: 'POST',
      body: {
        username: this.userNameRef.current.state.value?.trim(),
        userid: this.useridRef.current.state.value,
        id: this.idRef.current.state.value,
      },
    }).then(res => {
      if (res.code === 200) {
        // console.log(res.data)
        this.setState({ tableData: res.data })
      }
    })
  }

  render() {
    // 表格分页属性
    const paginationProps = {
      showSizeChanger: true,
      showQuickJumper: false,
      showTotal: () => `共${100}条`,
      pageSize: this.state.pageSize,
      current: 2,
      total: 100,
      // onShowSizeChange: (current,pageSize) => this.changePageSize(pageSize,current),
      // onChange: (current) => this.changePage(current),
    }

    return (
      <div>
        <Card style={{ marginTop: 20 }}>
          <Row>
            <Col span={6}>
              <span>用户名: </span>
              <Input style={{ display: 'inline', width: 200 }} ref={this.userNameRef}></Input>
            </Col>
            <Col span={6}>
              <span>userid: </span>
              <Input style={{ display: 'inline', width: 200 }} ref={this.useridRef}></Input>
            </Col>
            <Col span={6}>
              <span>id: </span>
              <Input style={{ display: 'inline', width: 200 }} ref={this.idRef}></Input>
            </Col>
            <Col span={6}>
              <Button
                type="primary"
                style={{ marginRight: 10 }}
                onClick={() => this.queryHistory()}
              >
                查询
              </Button>
              {/* <Button type="primary">重置</Button> */}
            </Col>
          </Row>
        </Card>
        <Card style={{ marginTop: 20 }}>
          <Table dataSource={this.state.tableData} rowKey="id">
            <Column title="ID" dataIndex="id" />
            <Column title="用户名" dataIndex="username" />
            <Column title="用户id" dataIndex="userid" />
            <Column
              title="需求问题"
              dataIndex="prompt"
              render={prompt => (
                <div dangerouslySetInnerHTML={{ __html: prompt.replaceAll('\n', '<br/>') }}></div>
              )}
            />
            <Column
              title="生成结果"
              dataIndex="result"
              render={result => (
                <div dangerouslySetInnerHTML={{ __html: result.replaceAll('\n', '<br/>') }}></div>
              )}
            />
            <Column title="类型" dataIndex="type" />
            {/* <Column title="token" dataIndex="token" /> */}

            {/* <Column
              title="Tags"
              dataIndex="tags"
              key="tags"
              render={tags => (
                <span>
                  {tags.map(tag => (
                    <Tag color="blue" key={tag}>
                      {tag}
                    </Tag>
                  ))}
                </span>
              )}
            />
            <Column
              title="Action"
              key="action"
              render={(text, record) => (
                <span>
                  <a>Invite {record.lastName}</a>
                  <Divider type="vertical" />
                  <a>Delete</a>
                </span>
              )}
            /> */}
          </Table>
        </Card>
      </div>
    )
  }
}

export default AIQuery
