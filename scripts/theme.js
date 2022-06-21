const LOCAL_STORAGE_KEY = 'minimal-dark-mode'

if (isDarkMode()) {
  document.getElementsByTagName('html')[0].classList.add('theme-dark')
}

function isDarkMode() {
  const storage = localStorage.getItem(LOCAL_STORAGE_KEY)
  return storage ? JSON.parse(storage) : false
}

function toggleDarkMode() {
  document.getElementsByTagName('html')[0].classList.toggle('theme-dark')
  const flippedDarkMode = !isDarkMode()
  localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(flippedDarkMode))
  return flippedDarkMode
}
