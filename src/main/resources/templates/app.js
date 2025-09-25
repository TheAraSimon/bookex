// --- Minimal SPA Router ---
const Router = (() => {
    const routes = {};
    function register(path, render) { routes[path] = render; }
    function go(hash) { location.hash = hash; }
    function parseHash() {
        const h = location.hash || '#/browse';
        const parts = h.split('/').filter(Boolean); // ['#','browse'] or ['#','detail','1']
        return { path:`#/${parts[1]||'browse'}`, parts };
    }
    function onChange() {
        const { path, parts } = parseHash();
        const render = routes[path] || routes['#/browse'];
        render(parts);
        updateAuthUI();
    }
    window.addEventListener('hashchange', onChange);
    return { register, go, onChange };
})();

// --- Data Store (localStorage) ---
const DB = (() => {
    const key = 'bookswap-db-v1';
    const state = JSON.parse(localStorage.getItem(key) || 'null') || seed();
    function save(){ localStorage.setItem(key, JSON.stringify(state)); }
    function seed(){
        const users = [
            {id:1,email:'alice@example.com',password:'x',displayName:'Alice',role:'USER',
                publicContact:true,preferredMethod:'EMAIL',contactEmail:'alice@example.com',contactPhone:'',createdAt:Date.now()},
            {id:2,email:'bob@example.com',password:'x',displayName:'Bob',role:'USER',
                publicContact:false,preferredMethod:'PHONE',contactEmail:'',contactPhone:'+15551234567',createdAt:Date.now()}
        ];
        const books = [
            {id:1,title:'The Hobbit',author:'J.R.R. Tolkien',isbn:'9780261103344',createdAt:Date.now()},
            {id:2,title:'Dune',author:'Frank Herbert',isbn:'9780441172719',createdAt:Date.now()},
            {id:3,title:'The Left Hand of Darkness',author:'Ursula K. Le Guin',isbn:'9780441478125',createdAt:Date.now()}
        ];
        const listings = [
            {id:1,bookId:1,ownerId:1,condition:'GOOD',notes:'Slightly worn dust jacket',available:true,createdAt:Date.now(),images:[demoImage('Hobbit')]},
            {id:2,bookId:2,ownerId:2,condition:'USED',notes:'Some notes in margins',available:true,createdAt:Date.now(),images:[demoImage('Dune'), demoImage('Dune 2')]},
        ];
        const swaps = []; // {id, listingId, requesterId, status, message, createdAt, updatedAt}
        const ratings = []; // {userId, bookId, difficulty, emotion, enjoyment}
        const currentUserId = null;
        return { users, books, listings, swaps, ratings, currentUserId, nextId:{listing:3,swap:1,book:4} };
    }
    function demoImage(text){
        const svg = `<svg xmlns='http://www.w3.org/2000/svg' width='640' height='360'>
    <rect width='100%' height='100%' fill='#e2e8f0'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='#111' font-family='sans-serif' font-size='28'>${text}</text></svg>`;
        return 'data:image/svg+xml;base64,'+btoa(unescape(encodeURIComponent(svg)));
    }

    // helpers
    function user(){ return state.users.find(u=>u.id===state.currentUserId)||null; }
    function byId(arr,id){ return arr.find(x=>x.id==id); }
    function bookAvg(bookId){
        const rs = state.ratings.filter(r=>r.bookId==bookId);
        if(!rs.length) return {difficulty:0,emotion:0,enjoyment:0,count:0};
        const s = rs.reduce((a,r)=>({difficulty:a.difficulty+r.difficulty,emotion:a.emotion+r.emotion,enjoyment:a.enjoyment+r.enjoyment}),{difficulty:0,emotion:0,enjoyment:0});
        return {difficulty:(s.difficulty/rs.length).toFixed(1),emotion:(s.emotion/rs.length).toFixed(1),enjoyment:(s.enjoyment/rs.length).toFixed(1),count:rs.length};
    }
    function myInbox(userId){
        const myListingIds = state.listings.filter(l=>l.ownerId==userId).map(l=>l.id);
        return state.swaps.filter(s=>myListingIds.includes(s.listingId));
    }
    function myOutbox(userId){
        return state.swaps.filter(s=>s.requesterId==userId);
    }
    return { state, save, user, byId, bookAvg, myInbox, myOutbox };
})();

// --- Utilities ---
function el(html){ const d=document.createElement('div'); d.innerHTML=html.trim(); return d.firstChild; }
function flash(msg){ const t=document.querySelector('#tpl-flash').content.firstElementChild.cloneNode(true); t.textContent=msg; document.body.appendChild(t); setTimeout(()=>t.remove(),2400); }
function requireAuth(){ if(!DB.user()){ flash('Please login'); Router.go('#/login'); return false; } return true; }
function fmtDate(ts){ const d = new Date(ts); return d.toLocaleString(); }
function badge(cond){ const c = cond.toLowerCase(); return `<span class="badge ${c==='good'?'good':c==='used'?'used':c==='worn'?'worn':'new'}">${cond}</span>`; }
function contactBlock(u, reveal){
    if(reveal || u.publicContact){
        if(u.preferredMethod==='EMAIL' && u.contactEmail) return `<div><strong>Contact:</strong> ${u.contactEmail}</div>`;
        if(u.preferredMethod==='PHONE' && u.contactPhone) return `<div><strong>Contact:</strong> ${u.contactPhone}</div>`;
        return `<div class="kv">Contact not configured</div>`;
    }
    return `<div class="kv">Contact hidden (will be revealed after acceptance)</div>`;
}

// --- Views ---
// Login
Router.register('#/login', () => {
    const root = document.getElementById('appRoot');
    const v = el(`
    <section class="card">
      <div class="h1">Login</div>
      <div class="row">
        <div class="field"><label>Email</label><input id="loginEmail" class="input" placeholder="alice@example.com"/></div>
        <div class="field"><label>Display Name</label><input id="loginName" class="input" placeholder="Alice"/></div>
        <button class="btn" id="doLogin">Login (Prototype)</button>
      </div>
      <div class="small kv">Prototype note: creates or reuses a user with this email.</div>
    </section>
  `);
    root.replaceChildren(v);
    v.querySelector('#doLogin').onclick = () => {
        const email = v.querySelector('#loginEmail').value.trim();
        const name = v.querySelector('#loginName').value.trim() || email.split('@')[0];
        if(!email){ flash('Enter email'); return; }
        let user = DB.state.users.find(u=>u.email===email);
        if(!user){
            user = { id: DB.state.users.length? Math.max(...DB.state.users.map(u=>u.id))+1 : 1,
                email, password:'x', displayName:name, role:'USER',
                publicContact:false, preferredMethod:null, contactEmail:'', contactPhone:'',
                createdAt:Date.now() };
            DB.state.users.push(user);
        } else { user.displayName = name; }
        DB.state.currentUserId = user.id; DB.save();
        flash(`Welcome, ${user.displayName}`);
        Router.go('#/browse');
    };
});

// Browse
Router.register('#/browse', () => {
    const root = document.getElementById('appRoot');
    const listings = DB.state.listings.filter(l=>l.available);
    const cards = listings.map(l=>{
        const book = DB.byId(DB.state.books, l.bookId);
        const owner = DB.byId(DB.state.users, l.ownerId);
        return `
    <div class="card">
      <div class="h3">${book.title}</div>
      <div class="kv">${book.author}</div>
      <div class="row" style="margin:6px 0;">${badge(l.condition)}</div>
      <div class="media">${(l.images||[]).slice(0,1).map(src=>`<div class="thumb"><img src="${src}" alt="cover"/></div>`).join('')}</div>
      <div class="small kv">Owner: ${owner.displayName}</div>
      <div class="actions">
        <button class="btn btn-outline" onclick="Router.go('#/detail/${l.id}')">View</button>
        <button class="btn" onclick="Actions.requestSwap(${l.id})">Request</button>
      </div>
    </div>`;
    }).join('');
    root.innerHTML = `
    <section class="section">
      <div class="h1">Browse Listings</div>
      <div class="grid cards">${cards || '<div class="card">No listings yet.</div>'}</div>
    </section>
  `;
});

// Detail
Router.register('#/detail', (parts) => {
    const id = parts[2]; const listing = DB.byId(DB.state.listings, id);
    const root = document.getElementById('appRoot');
    if(!listing){ root.innerHTML = '<div class="card">Listing not found</div>'; return; }
    const book = DB.byId(DB.state.books, listing.bookId);
    const owner = DB.byId(DB.state.users, listing.ownerId);
    const me = DB.user();
    const avg = DB.bookAvg(book.id);
    const canRequest = !!me && me.id !== owner.id && listing.available;
    root.innerHTML = `
    <section class="card">
      <div class="row" style="justify-content:space-between;align-items:flex-start">
        <div>
          <div class="h1">${book.title}</div>
          <div class="kv">${book.author}</div>
          <div style="margin:8px 0">${badge(listing.condition)}</div>
        </div>
        <div class="small">Listing #${listing.id}</div>
      </div>

      <div class="media" style="margin:8px 0">${(listing.images||[]).map(src=>`<div class="thumb"><img src="${src}"/></div>`).join('')}</div>

      <div class="section">
        <div class="h3">Notes</div>
        <div>${listing.notes || '<span class="kv">No notes</span>'}</div>
      </div>

      <div class="section">
        <div class="h3">Owner</div>
        <div>${owner.displayName}</div>
        ${contactBlock(owner, false)}
      </div>

      <div class="section">
        <div class="h3">Ratings (avg of ${avg.count})</div>
        <div class="row">
          <span>Difficulty: ${avg.difficulty}</span>
          <span>Emotion: ${avg.emotion}</span>
          <span>Enjoyment: ${avg.enjoyment}</span>
        </div>
        ${me? ratingForm(me.id, book.id) : '<div class="kv">Login to rate</div>'}
      </div>

      <div class="actions">
        <button class="btn" ${canRequest?'':'disabled'} onclick="Actions.requestSwap(${listing.id})">Request Swap</button>
        ${!listing.available? '<span class="kv">Not available</span>':''}
      </div>
    </section>
  `;
    wireRatingHandlers();
});

function ratingForm(userId, bookId){
    const existing = DB.state.ratings.find(r=>r.userId===userId && r.bookId===bookId) || {difficulty:3,emotion:3,enjoyment:3};
    return `
  <div class="row" style="align-items:baseline; gap:16px">
    <label>Difficulty <input type="range" min="1" max="5" value="${existing.difficulty}" id="rateDiff"></label>
    <label>Emotion <input type="range" min="1" max="5" value="${existing.emotion}" id="rateEmo"></label>
    <label>Enjoyment <input type="range" min="1" max="5" value="${existing.enjoyment}" id="rateJoy"></label>
    <button class="btn btn-secondary" id="saveRating">Save Rating</button>
  </div>`;
}
function wireRatingHandlers(){
    const btn = document.getElementById('saveRating'); if(!btn) return;
    btn.onclick = () => {
        const [diff, emo, joy] = ['rateDiff','rateEmo','rateJoy'].map(id=>+document.getElementById(id).value);
        const me = DB.user(); const bookId = DB.byId(DB.state.listings, location.hash.split('/')[2]).bookId;
        let r = DB.state.ratings.find(x=>x.userId===me.id && x.bookId===bookId);
        if(!r){ DB.state.ratings.push({userId:me.id,bookId, difficulty:diff,emotion:emo,enjoyment:joy,createdAt:Date.now(),updatedAt:Date.now()}); }
        else { r.difficulty=diff; r.emotion=emo; r.enjoyment=joy; r.updatedAt=Date.now(); }
        DB.save(); flash('Rating saved'); Router.onChange();
    };
}

// Library
Router.register('#/library', () => {
    if(!requireAuth()) return;
    const root = document.getElementById('appRoot');
    const me = DB.user();
    const myListings = DB.state.listings.filter(l=>l.ownerId===me.id);
    root.innerHTML = `
    <section class="section">
      <div class="row" style="justify-content:space-between">
        <div class="h1">My Library</div>
        <button class="btn" id="addListing">+ Add Listing</button>
      </div>
      <table class="table">
        <thead><tr><th>Book</th><th>Condition</th><th>Available</th><th>Actions</th></tr></thead>
        <tbody>
          ${myListings.map(l=>{
        const b = DB.byId(DB.state.books,l.bookId);
        return `<tr>
              <td>${b.title} <span class="kv">by ${b.author}</span></td>
              <td>${badge(l.condition)}</td>
              <td>${l.available? 'Yes':'No'}</td>
              <td class="row">
                <button class="btn btn-secondary" onclick="UI.editListing(${l.id})">Edit</button>
                <button class="btn btn-danger" onclick="Actions.deleteListing(${l.id})">Delete</button>
              </td></tr>`;
    }).join('') || `<tr><td colspan="4">No listings yet</td></tr>`}
        </tbody>
      </table>
    </section>
    <section id="editor" class="card hidden"></section>
  `;
    document.getElementById('addListing').onclick = ()=>UI.editListing(null);
});

// Swaps
Router.register('#/swaps', () => {
    if(!requireAuth()) return;
    const root = document.getElementById('appRoot'); const me = DB.user();
    const inbox = DB.myInbox(me.id); const outbox = DB.myOutbox(me.id);
    function row(s, asOwner){
        const l = DB.byId(DB.state.listings, s.listingId);
        const b = DB.byId(DB.state.books, l.bookId);
        const other = asOwner ? DB.byId(DB.state.users, s.requesterId) : DB.byId(DB.state.users, l.ownerId);
        const canAccept = asOwner && s.status==='PENDING';
        const canComplete = s.status==='ACCEPTED';
        const revealContacts = s.status==='ACCEPTED';
        return `<tr>
      <td>#${s.id}</td>
      <td>${b.title}</td>
      <td>${other.displayName}</td>
      <td>${s.status}</td>
      <td class="row">
        ${canAccept? `<button class="btn" onclick="Actions.acceptSwap(${s.id})">Accept</button>
                      <button class="btn btn-secondary" onclick="Actions.declineSwap(${s.id})">Decline</button>`:''}
        ${canComplete? `<button class="btn btn-secondary" onclick="Actions.completeSwap(${s.id})">Mark Completed</button>
                        <button class="btn btn-danger" onclick="Actions.cancelSwap(${s.id})">Cancel</button>`:''}
        <button class="btn btn-outline" onclick="UI.openSwap(${s.id})">Open</button>
      </td>
    </tr>
    <tr><td colspan="5">
      <div class="card">
        <div class="small kv">Created: ${fmtDate(s.createdAt)} • Updated: ${fmtDate(s.updatedAt)}</div>
        <div class="row">
          <div style="flex:1">
            <div class="h3">Owner Contact</div>
            ${contactBlock(DB.byId(DB.state.users, l.ownerId), revealContacts)}
          </div>
          <div style="flex:1">
            <div class="h3">Requester Contact</div>
            ${contactBlock(DB.byId(DB.state.users, s.requesterId), revealContacts)}
          </div>
        </div>
      </div>
    </td></tr>`;
    }
    root.innerHTML = `
    <section class="section">
      <div class="h1">My Swaps</div>
      <div class="h2">Inbox (for my listings)</div>
      <table class="table">
        <thead><tr><th>ID</th><th>Book</th><th>From</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>${inbox.map(s=>row(s,true)).join('') || '<tr><td colspan="5">No inbox items</td></tr>'}</tbody>
      </table>
    </section>
    <section class="section">
      <div class="h2">Outbox (my requests)</div>
      <table class="table">
        <thead><tr><th>ID</th><th>Book</th><th>To</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>${outbox.map(s=>row(s,false)).join('') || '<tr><td colspan="5">No outbox items</td></tr>'}</tbody>
      </table>
    </section>
  `;
});

// Profile
Router.register('#/profile', () => {
    if(!requireAuth()) return;
    const me = DB.user(); const root = document.getElementById('appRoot');
    root.innerHTML = `
    <section class="card">
      <div class="h1">Profile</div>
      <div class="row">
        <div class="field" style="flex:1">
          <label>Display Name</label>
          <input id="pName" class="input" value="${me.displayName}"/>
        </div>
        <div class="field">
          <label>Public Contact</label>
          <input id="pPublic" type="checkbox" ${me.publicContact?'checked':''}/> Show my contact on listing pages
        </div>
      </div>
      <div class="row">
        <div class="field">
          <label>Preferred Method</label>
          <select id="pMethod" class="select">
            <option value="">(none)</option>
            <option value="EMAIL" ${me.preferredMethod==='EMAIL'?'selected':''}>Email</option>
            <option value="PHONE" ${me.preferredMethod==='PHONE'?'selected':''}>Phone</option>
          </select>
        </div>
        <div class="field"><label>Email</label><input id="pEmail" class="input" value="${me.contactEmail||''}" placeholder="you@example.com"/></div>
        <div class="field"><label>Phone (E.164)</label><input id="pPhone" class="input" value="${me.contactPhone||''}" placeholder="+15551234567"/></div>
      </div>
      <div class="actions">
        <button class="btn" id="saveProfile">Save Profile</button>
      </div>
      <div class="small kv">Contact will be revealed to counterparties when a swap is ACCEPTED.</div>
    </section>
  `;
    document.getElementById('saveProfile').onclick = ()=>{
        me.displayName = document.getElementById('pName').value.trim() || me.displayName;
        me.publicContact = document.getElementById('pPublic').checked;
        me.preferredMethod = document.getElementById('pMethod').value || null;
        me.contactEmail = document.getElementById('pEmail').value.trim();
        me.contactPhone = document.getElementById('pPhone').value.trim();
        DB.save(); flash('Profile saved'); Router.onChange();
    };
});

// Listing editor (inline)
const UI = {
    editListing(id){
        const root = document.getElementById('editor');
        const me = DB.user(); root.classList.remove('hidden');
        const l = id? DB.byId(DB.state.listings, id) : {id:null, bookId:'', ownerId:me.id, condition:'GOOD', notes:'', available:true, images:[]};
        const b = l.id? DB.byId(DB.state.books, l.bookId) : {title:'',author:'',isbn:''};
        root.innerHTML = `
      <div class="h2">${id? 'Edit Listing':'Add Listing'}</div>
      <div class="row">
        <div class="field" style="flex:1"><label>Title</label><input id="eTitle" class="input" value="${b.title}"/></div>
        <div class="field" style="flex:1"><label>Author</label><input id="eAuthor" class="input" value="${b.author}"/></div>
        <div class="field"><label>ISBN</label><input id="eIsbn" class="input" value="${b.isbn||''}"/></div>
      </div>
      <div class="row">
        <div class="field">
          <label>Condition</label>
          <select id="eCond" class="select">
            ${['NEW','GOOD','USED','WORN'].map(c=>`<option ${l.condition===c?'selected':''}>${c}</option>`).join('')}
          </select>
        </div>
        <div class="field">
          <label>Available</label>
          <input id="eAvail" type="checkbox" ${l.available?'checked':''}/>
        </div>
      </div>
      <div class="field"><label>Notes</label><textarea id="eNotes" class="textarea">${l.notes||''}</textarea></div>
      <div class="field">
        <label>Images (up to 3, preview only)</label>
        <input id="eImgs" type="file" accept="image/*" multiple />
        <div class="media" id="ePreview">${(l.images||[]).map(src=>`<div class="thumb"><img src="${src}"/></div>`).join('')}</div>
      </div>
      <div class="actions">
        <button class="btn" onclick="Actions.saveListing(${id||'null'})">${id? 'Save':'Create'}</button>
        <button class="btn btn-secondary" onclick="document.getElementById('editor').classList.add('hidden')">Close</button>
      </div>
    `;
        const input = root.querySelector('#eImgs');
        input.onchange = async () => {
            const files = Array.from(input.files).slice(0,3);
            const imgs = [];
            for(const f of files){
                const data = await f.arrayBuffer();
                const base64 = btoa(String.fromCharCode(...new Uint8Array(data)));
                imgs.push(`data:${f.type};base64,${base64}`);
            }
            root.querySelector('#ePreview').innerHTML = imgs.map(src=>`<div class="thumb"><img src="${src}"/></div>`).join('');
            root.dataset.newImages = JSON.stringify(imgs);
        };
    },
    openSwap(id){
        Router.go('#/swaps'); setTimeout(()=>{ flash(`Opened swap #${id}`); }, 10);
    }
};

// Actions
const Actions = {
    requestSwap(listingId){
        if(!requireAuth()) return;
        const me = DB.user(); const l = DB.byId(DB.state.listings, listingId);
        if(me.id===l.ownerId){ flash('You own this listing'); return; }
        if(!l.available){ flash('Listing not available'); return; }
        const already = DB.state.swaps.find(s=>s.listingId===listingId && s.requesterId===me.id && s.status!=='CANCELLED' && s.status!=='DECLINED');
        if(already){ flash('You already have a request for this listing'); return; }
        const s = { id: DB.state.nextId.swap++, listingId, requesterId: me.id, status:'PENDING', message:'Interested in swapping!', createdAt:Date.now(), updatedAt:Date.now() };
        DB.state.swaps.unshift(s); DB.save();
        flash('Swap request sent! Check My Swaps → Outbox'); Router.go('#/swaps');
    },
    acceptSwap(id){
        const s = DB.byId(DB.state.swaps, id); const me = DB.user();
        const l = DB.byId(DB.state.listings, s.listingId);
        if(l.ownerId!==me.id){ flash('Only owner can accept'); return; }
        s.status='ACCEPTED'; s.updatedAt=Date.now(); l.available=false; DB.save();
        flash('Accepted. Contacts revealed.'); Router.onChange();
    },
    declineSwap(id){
        const s = DB.byId(DB.state.swaps, id); const me = DB.user();
        const l = DB.byId(DB.state.listings, s.listingId);
        if(l.ownerId!==me.id){ flash('Only owner can decline'); return; }
        s.status='DECLINED'; s.updatedAt=Date.now(); DB.save();
        flash('Declined.'); Router.onChange();
    },
    completeSwap(id){
        const s = DB.byId(DB.state.swaps, id); const me = DB.user();
        const l = DB.byId(DB.state.listings, s.listingId);
        const isParticipant = (l.ownerId===me.id || s.requesterId===me.id);
        if(!isParticipant){ flash('Only participants can complete'); return; }
        s.status='COMPLETED'; s.updatedAt=Date.now(); DB.save();
        flash('Swap marked completed'); Router.onChange();
    },
    cancelSwap(id){
        const s = DB.byId(DB.state.swaps, id); const me = DB.user();
        if(s.requesterId!==me.id){ flash('Only requester can cancel'); return; }
        s.status='CANCELLED'; s.updatedAt=Date.now(); DB.save();
        flash('Swap cancelled'); Router.onChange();
    },
    deleteListing(id){
        if(!confirm('Delete this listing?')) return;
        const idx = DB.state.listings.findIndex(x=>x.id===id);
        if(idx>=0){ DB.state.listings.splice(idx,1); DB.save(); flash('Listing deleted'); Router.onChange(); }
    },
    saveListing(id){
        const me = DB.user();
        const root = document.getElementById('editor');
        const t = root.querySelector('#eTitle').value.trim();
        const a = root.querySelector('#eAuthor').value.trim();
        if(!t || !a){ flash('Title & Author required'); return; }
        let book;
        // reuse book if same title+author exists
        book = DB.state.books.find(b=>b.title===t && b.author===a);
        if(!book){
            book = {id:DB.state.nextId.book++, title:t, author:a, isbn:root.querySelector('#eIsbn').value.trim(), createdAt:Date.now()};
            DB.state.books.push(book);
        }
        const imgs = root.dataset.newImages ? JSON.parse(root.dataset.newImages) : null;
        if(id){
            const l = DB.byId(DB.state.listings, id);
            l.bookId = book.id;
            l.condition = root.querySelector('#eCond').value;
            l.notes = root.querySelector('#eNotes').value.trim();
            l.available = root.querySelector('#eAvail').checked;
            if(imgs) l.images = imgs;
        } else {
            DB.state.listings.unshift({
                id: DB.state.nextId.listing++,
                bookId: book.id,
                ownerId: me.id,
                condition: root.querySelector('#eCond').value,
                notes: root.querySelector('#eNotes').value.trim(),
                available: root.querySelector('#eAvail').checked,
                createdAt: Date.now(),
                images: imgs || []
            });
        }
        DB.save(); flash('Listing saved'); Router.onChange(); root.classList.add('hidden');
    }
};

// Top bar auth controls
function updateAuthUI(){
    const who = document.getElementById('whoami');
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const me = DB.user();
    if(me){
        who.textContent = `Hi, ${me.displayName}`;
        loginBtn.style.display='none';
        logoutBtn.style.display='inline-flex';
    } else {
        who.textContent = '';
        loginBtn.style.display='inline-flex';
        logoutBtn.style.display='none';
    }
}
document.getElementById('loginBtn').onclick = ()=>Router.go('#/login');
document.getElementById('logoutBtn').onclick = ()=>{
    DB.state.currentUserId = null; DB.save(); flash('Logged out'); Router.onChange();
};

// Start
Router.onChange();
