import React from 'react'
import { Modal, Form, Input, Radio, Upload, Button, Icon } from 'antd'
import axios from 'axios'

const ImageModal = props => {
  const defaultObj = props.minder.queryCommandValue('Image')
  const { getFieldDecorator, getFieldValue, setFieldsValue } = props.form

  const onOk = () => {
    const { form, minder, onCancel } = props
    form.validateFields((err, values) => {
      if (err) {
        // console.log('Received values of form: ', values)
        return
      }
      const params = { ...values }
      if (params.url) {
        minder.execCommand('image', params.url, params.title)
      } else {
        uploadImageToPublic(params.upload[0].originFileObj, params.title)
      }
      // console.log('imageUrl info ==',imageUrl)
      // minder.execCommand('image', imageUrl, params.title)
      setTimeout(() => {
        onCancel()
      }, 300)
    })
  }
  const normFile = e => {
    if (Array.isArray(e)) {
      return e
    }
    if (e) {
      const fileList = e.file.status === 'removed' ? [] : [e.file]
      return e && fileList
    }
  }
  const onImageChange = e => {
    if (e.file.status === 'done') {
      const { response = {} } = e.file
      setFieldsValue({ url: response.data ? response.data[0].url : '' })
    }
  }

  const uploadImageToPublic = (file, title) => {
    // console.log('file info ==',file)
    const { minder } = props
    let fileName = file.name.split('.')
    const formData = new FormData()
    formData.append('file', file)
    formData.append('bizId', 'minder')
    formData.append('bucketType', 'public')
    formData.append('fileType', '.' + fileName[fileName.length - 1])
    const FILEGW_HOST = 'https://file-gw.test.shantaijk.cn/file/uploadFileReturnKey'
    const instance = axios.create({
      withCredentials: true,
    })
    instance
      .post(FILEGW_HOST, formData)
      .then(response => {
        // console.log('上传成功 response==', response)
        let imageUrl =
          'https://file-gw.test.shantaijk.cn/file/publicPicByKey/' + response.data.result
        minder.execCommand('image', imageUrl, title)
      })
      .catch(error => {
        // eslint-disable-next-line no-console
        console.log(error)
      })
  }
  const onTypeChange = value => {
    if (value === 'upload') {
      getFieldDecorator('url')
    }
    return value
  }

  return (
    <Modal
      title="图片"
      className="agiletc-modal"
      visible={props.visible}
      onOk={onOk}
      onCancel={props.onCancel}
    >
      <Form layout="vertical">
        <Form.Item>
          {getFieldDecorator('type', {
            initialValue: 'out',
            normalize: onTypeChange,
          })(
            <Radio.Group>
              <Radio.Button value="out">外链图片</Radio.Button>
              <Radio.Button value="upload">上传图片</Radio.Button>
            </Radio.Group>,
          )}
        </Form.Item>
        {getFieldValue('type') === 'out' ? (
          <Form.Item label="图片地址">
            {getFieldDecorator('url', {
              rules: [
                {
                  required: true,
                  message: '必填：以 http(s):// 或 ftp:// 开头',
                },
              ],
              initialValue: defaultObj.url,
            })(<Input placeholder="必填：以 http(s):// 或 ftp:// 开头" />)}
          </Form.Item>
        ) : (
          <Form.Item label="上传图片">
            {getFieldDecorator('upload', {
              rules: [{ required: true, message: '请上传图片！' }],
              valuePropName: 'fileList',
              normalize: normFile,
            })(
              <Upload
                // action={baseUrl + uploadUrl}
                listType="picture"
                accept="image/*"
                withCredentials
                // customRequest={data => {
                //   uploadImageToPublic(data.file)
                // }}
              >
                <Button>
                  <Icon type="upload" /> 点击上传
                </Button>
              </Upload>,
            )}
          </Form.Item>
        )}

        <Form.Item label="提示文本">
          {getFieldDecorator('title', {
            initialValue: defaultObj.title,
          })(<Input placeholder="选填：鼠标在图片上悬停时提示的文本" />)}
        </Form.Item>
      </Form>
    </Modal>
  )
}
const WrappedImageForm = Form.create({ name: 'image' })(ImageModal)
export default WrappedImageForm
