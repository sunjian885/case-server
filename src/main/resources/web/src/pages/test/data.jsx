import React from 'react'
import { Button, message } from 'antd'
// import utils from '@/utils'
class Data extends React.PureComponent {
  constructor(props) {
    super(props)
    // eslint-disable-next-line no-unused-vars
    this.state = {
      dataSource: window.openDatabase('myapp.db', 1, 'windos', 5 * 1024 * 1024),
    }
    this.initDB()
  }

  initDB = () => {
    // let dataSource = window.openDatabase('myapp.db', 1, 'windos', 5 * 1024 * 1024);
    this.state.dataSource.transaction(tx => {
      tx.executeSql(
        'create table if not exists mydata(id varchar(32), name varchar(5242880))',
        [],
        (tx, result) => {
          alert('创建user表成功:' + result)
        },
        (tx, error) => {
          alert('创建user表失败:' + error.message)
        },
      )
    })
  }

  insertData = () => {
    this.state.dataSource.transaction(tx => {
      tx.executeSql(
        'insert into mydata (id, name) values(?,?)',
        [1, '刘德华'],
        (tx, result) => {
          alert('插入表成功:' + result)
        },
        (tx, error) => {
          alert('插入表失败:' + error.message)
        },
      )
    })
  }

  deleteData = () => {
    this.state.dataSource.transaction(tx => {
      tx.executeSql(
        'delete from mydata where id = ?',
        [1],
        (tx, result) => {
          alert('删除数据成功:' + result)
        },
        (tx, error) => {
          alert('删除数据失败:' + error.message)
        },
      )
    })
  }

  selectData = () => {
    this.state.dataSource.transaction(tx => {
      tx.executeSql(
        'select * from mydata',
        [],
        (tx, result) => {
          // eslint-disable-next-line no-console
          console.log('查询数据', result)
        },
        (tx, error) => {
          alert('查询表失败:' + error.message)
        },
      )
    })
  }

  componentDidMount() {}

  render() {
    return (
      <div>
        <Button type="primary" onClick={() => this.insertData()}>
          存入数据
        </Button>
        <Button type="primary" onClick={() => this.deleteData()}>
          删除数据
        </Button>
        <Button type="primary" onClick={() => this.selectData()}>
          查询数据
        </Button>
      </div>
    )
  }
}

export default Data
