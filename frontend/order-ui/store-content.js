const selectors = {
  hero_tag: '.hero-copy .eyebrow', hero_description: '.hero-copy p:nth-of-type(2)',
  hero_index: '.hero-index', collection_tag: '.collection-heading .eyebrow',
  collection_description: '.collection-heading p:nth-of-type(2)',
  editorial_tag: '.editorial .eyebrow', editorial_description: '.editorial p:nth-of-type(2)',
  delivery_estimate: '#delivery-view .choice small', size_intro: '#size-dialog > p'
};

function replaceText(selector, value) {
  const element = document.querySelector(selector);
  if (element && value !== undefined) element.textContent = value;
}

function replaceTitle(selector, first, second, secondSelector) {
  const title = document.querySelector(selector);
  if (!title || first === undefined || second === undefined) return;
  title.firstChild.nodeValue = first;
  const accent = title.querySelector(secondSelector);
  if (accent) accent.textContent = second;
  else title.lastChild.nodeValue = second;
}

async function loadStoreContent() {
  const response = await fetch('/backend/order/api/shop/content');
  const result = await response.json();
  if (!response.ok || result.success === false) throw new Error('Không tải được nội dung cửa hàng.');
  const content = result.data;
  if (!content) throw new Error('Nội dung cửa hàng trống.');
  if (content.announcement) document.querySelector('.announcement').firstChild.nodeValue = content.announcement + ' ';
  for (const [key, selector] of Object.entries(selectors)) replaceText(selector, content[key]);
  replaceTitle('.hero-copy h1', content.hero_title, content.hero_accent, 'em');
  replaceTitle('.collection-heading h2', content.collection_title, content.collection_accent, 'em');
  replaceTitle('.editorial h2', content.editorial_title, content.editorial_subtitle, 'em');
  if (content.size_guide) {
    const body = document.querySelector('#size-dialog tbody');
    body.replaceChildren();
    for (const line of content.size_guide.split('\n')) {
      const cells = line.split('|');
      if (cells.length !== 4) continue;
      const row = document.createElement('tr');
      for (const cell of cells) {
        const td = document.createElement('td');
        td.textContent = cell.trim();
        row.append(td);
      }
      body.append(row);
    }
  }
}

loadStoreContent().catch(error => {
  console.error(error);
  const message = document.createElement('p');
  message.className = 'error';
  message.textContent = 'Không tải được nội dung cửa hàng. Vui lòng tải lại trang.';
  document.querySelector('.hero-copy').prepend(message);
});
