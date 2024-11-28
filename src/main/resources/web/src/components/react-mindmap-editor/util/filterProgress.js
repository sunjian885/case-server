import jsonDiff from 'fast-json-patch'

//给没有progress的叶子节点赋值progress=99，并且将当前节点的id和他的父id的值放在
export const setNoneToProgress99 = value => {
  if (value?.children.length > 0) {
    value.children.forEach(item => {
      item.id = item.data.id
      item.parentId = value.data.id
      setNoneToProgress99(item)
    })
  } else {
    if (
      !value.data.hasOwnProperty('progress') ||
      (value.data.hasOwnProperty('progress') && value.data.progress == null)
    ) {
      value.data.progress = 99
    }
  }
}

export const clearProgress99ToNone = value => {
  if (value?.children.length > 0) {
    value.children.forEach(item => {
      if (item.hasOwnProperty('id')) {
        delete item['id']
      }
      if (item.hasOwnProperty('parentId')) {
        delete item['parentId']
      }
      clearProgress99ToNone(item)
    })
  } else {
    if (value.data.hasOwnProperty('progress') && value.data.progress == 99) {
      if (value.hasOwnProperty('id')) {
        delete value['id']
      }
      if (value.hasOwnProperty('parentId')) {
        delete value['parentId']
      }
      delete value.data['progress']
    }
  }
}

//树转数组
export const TreeToArr = tree => {
  let result = [] // 结果
  function getPath(node, arr) {
    arr.push(node)
    if (node.children.length > 0) {
      // 存在多个节点就递归
      node.children.forEach(v2 => getPath(v2, [...arr]))
    } else {
      result.push(arr)
    }
  }
  //tree.forEach(v => getPath(v, []))
  getPath(tree, [])
  return result
}

//转换二维数组成为一维数组，去掉数组中的重复节点
// 入参是二维数组
export const transferTwoArrayToArray = twoArray => {
  let map = new Map()
  twoArray.forEach(array => {
    array.forEach(item => map.set(item.id, item))
  })
  return [...map.values()]
}

export const transferArrayToJSON = (rootDataArray, dataId) => {
  return rootDataArray
    .filter(item => item.parentId == dataId)
    .map(item => ({ ...item, children: transferArrayToJSON(rootDataArray, item.id) }))
}

export const filterAction = (caseContentJson, filterProgresses) => {
  let rootData = caseContentJson.root
  rootData.id = rootData.data.id
  setNoneToProgress99(rootData)
  // console.log('rootData ====', rootData)

  //将原始数组转成二维数组
  let result = TreeToArr({ ...rootData })

  /**
   * 倒序遍历数组，对比最后一个元素，如果最后一个元素不包含我们需要的执行状态，就删除掉当前
   */
  for (let i = result.length - 1; i >= 0; i--) {
    let item = result[i]
    if (
      !(
        item[item.length - 1].data.hasOwnProperty('progress') &&
        filterProgresses.includes(item[item.length - 1].data.progress)
      )
    ) {
      result.splice(i, 1)
    }
  }
  //过滤后啥也没有，就直接返回空字符串
  if (result.length === 0) {
    return null
  }
  //转换二维数组成为一维数组，去掉数组中的重复节点
  let rootDataArray = transferTwoArrayToArray(result)
  //将json数组转成json对象
  let rootDataJson = transferArrayToJSON(rootDataArray, undefined)[0]
  clearProgress99ToNone(rootDataJson)
  caseContentJson.root = rootDataJson
  return caseContentJson
}

//通过jsondiff工具给缓存中的caseContent更新
export const progressApplyPatch = (sessionKey, patch) => {
  let sessionValue = sessionStorage.getItem(sessionKey)
  if (sessionValue != null) {
    let jsonPatchs = convertPatchToJsonPatch(patch)
    let newContent = jsonDiff.applyPatch(JSON.parse(sessionValue), jsonPatchs).newDocument
    sessionStorage.setItem(sessionKey, JSON.stringify(newContent))
    return newContent
  } else {
    return
  }
}

const convertPatchToJsonPatch = patch => {
  let jsonPatchs = []
  patch.forEach(item => {
    let { op, path, value } = item
    let jsonPatchItem = { op, path, value }
    jsonPatchs.push(jsonPatchItem)
  })
  return jsonPatchs
}

//筛选出所有的节点
const filterAllNodes = rootNode => {
  let allNodes = []
  function filter(filterNode) {
    allNodes.push(filterNode.data.id)
    if (filterNode.hasOwnProperty('children') && filterNode.children.length > 0) {
      //是根节点的话就将id放入到数组中
      filterNode.children.forEach(item => {
        filter(item)
      })
    }
  }
  filter(rootNode)
  return allNodes
}

//保留指定的叶子节点
const reserveAssignNodes = (newContentRoot, allNodes) => {
  if (newContentRoot.hasOwnProperty('children') && newContentRoot.children.length > 0) {
    //是根节点的话就判断是否包含在leafNodes中，如果不包含就删除掉当前节点
    let childrens = newContentRoot.children
    for (let i = childrens.length - 1; i >= 0; i--) {
      let item = childrens[i]
      if (!allNodes.includes(item.data.id)) {
        childrens.splice(i, 1)
      } else {
        reserveAssignNodes(item, allNodes)
      }
    }
  }
}

/**
 * 找到orgin中的JSON对象中的叶子节点，然后filter newContentJson中的节点
 * @param {json object} newContentJson 新的用例JSON对象
 * @param {json object} orginContentJson minder中的原始JSON对象
 */
export const filterNodeFromMinder = (newContentJson, orginContentJson) => {
  let rootNode = orginContentJson.root
  let allNodes = filterAllNodes(rootNode)
  let newContentRoot = newContentJson.root
  reserveAssignNodes(newContentRoot, allNodes)
  return newContentJson
}
